package com.xuyouyu.harness.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyList;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.mockito.Mockito;

import com.xuyouyu.harness.agent.AgentPlanner;
import com.xuyouyu.harness.agent.FinalAnswer;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AgentPlanner planner;

    private final JsonMapper jsonMapper = new JsonMapper();

    @Test
    void rejectsBlankPromptAndUnknownRun() throws Exception {
        mockMvc.perform(post("/api/harness/runs")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"prompt\":\"   \"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/harness/runs/missing"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/harness/runs/missing/events"))
                .andExpect(status().isNotFound());
    }

    @Test
    void postReturnsBeforeAgentFinishesAndGetShowsFinalAnswer() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Mockito.when(planner.plan(anyString(), anyList())).thenAnswer(invocation -> {
            started.countDown();
            if (!release.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("测试闸门等待超时");
            }
            return new FinalAnswer("离线回答");
        });

        String runId;
        try {
            runId = submitRun("请给出简短回答");
            Assertions.assertTrue(started.await(5, TimeUnit.SECONDS), "后台任务没有启动");
            Assertions.assertEquals("RUNNING", getRun(runId).path("status").stringValue());
        } finally {
            release.countDown();
        }

        JsonNode finished = awaitStatus(runId, "SUCCEEDED");
        Assertions.assertEquals("离线回答", finished.path("result").stringValue());
        JsonNode events = awaitLastEvent(runId, "SUCCEEDED");
        Assertions.assertEquals("ACCEPTED", events.get(0).path("type").stringValue());
        Assertions.assertEquals("SUCCEEDED", events.get(events.size() - 1).path("type").stringValue());
    }

    @Test
    void getShowsFailedWhenAgentThrows() throws Exception {
        Mockito.when(planner.plan(anyString(), anyList()))
                .thenThrow(new IllegalStateException("模拟规划失败"));

        String runId = submitRun("触发失败");
        JsonNode finished = awaitStatus(runId, "FAILED");
        Assertions.assertTrue(finished.path("result").isNull());
        Assertions.assertEquals("Agent 执行失败，请查看服务日志", finished.path("errorMessage").stringValue());
    }

    private String submitRun(String prompt) throws Exception {
        String body = jsonMapper.writeValueAsString(java.util.Map.of("prompt", prompt));
        MvcResult result = mockMvc.perform(post("/api/harness/runs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isAccepted())
                .andReturn();
        JsonNode response = jsonMapper.readTree(result.getResponse().getContentAsString());
        String runId = response.path("runId").stringValue();
        Assertions.assertNotNull(runId);
        Assertions.assertEquals("ACCEPTED", response.path("status").stringValue());
        return runId;
    }

    private JsonNode getRun(String runId) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/harness/runs/{runId}", runId))
                .andExpect(status().isOk())
                .andReturn();
        return jsonMapper.readTree(result.getResponse().getContentAsString());
    }

    private JsonNode awaitStatus(String runId, String expectedStatus) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        JsonNode last = null;
        while (System.nanoTime() < deadline) {
            last = getRun(runId);
            if (expectedStatus.equals(last.path("status").stringValue())) {
                return last;
            }
            Thread.sleep(25);
        }
        Assertions.fail("等待状态 " + expectedStatus + " 超时，最后状态："
                + (last == null ? "无" : last.path("status").stringValue()));
        return last;
    }

    private JsonNode awaitLastEvent(String runId, String expectedType) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            MvcResult result = mockMvc.perform(get("/api/harness/runs/{runId}/events", runId))
                    .andExpect(status().isOk())
                    .andReturn();
            JsonNode events = jsonMapper.readTree(result.getResponse().getContentAsString());
            if (!events.isEmpty()
                    && expectedType.equals(events.get(events.size() - 1).path("type").stringValue())) {
                return events;
            }
            Thread.sleep(25);
        }
        Assertions.fail("等待事件 " + expectedType + " 超时");
        return null;
    }
}
