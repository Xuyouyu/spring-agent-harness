package com.xuyouyu.harness.llm;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.xuyouyu.harness.agent.AgentDecision;
import com.xuyouyu.harness.agent.AgentRunner;
import com.xuyouyu.harness.agent.FinalAnswer;
import com.xuyouyu.harness.agent.ToolCall;
import com.xuyouyu.harness.agent.ToolExecutionResult;
import com.xuyouyu.harness.tool.HarnessTool;
import com.xuyouyu.harness.tool.LogErrorCountTool;
import com.xuyouyu.harness.tool.ToolRegistry;

class DeepSeekAgentPlannerTests {

    @Test
    void sendsFullToolHistoryToModel() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        DeepSeekAgentPlanner planner = new DeepSeekAgentPlanner(builder, "test-key",
                new DeepSeekDecisionMapper(), new ToolRegistry(List.of(new LogErrorCountTool())));
        List<ToolExecutionResult> history = List.of(
                new ToolExecutionResult(new ToolCall("call_1", "log_error_count", "ERROR A"), "1"),
                new ToolExecutionResult(new ToolCall("call_2", "log_error_count", "ERROR B"), "1"));

        server.expect(requestTo("https://api.deepseek.com/chat/completions"))
                .andExpect(jsonPath("$.messages.length()").value(5))
                .andExpect(jsonPath("$.messages[2].tool_call_id").value("call_1"))
                .andExpect(jsonPath("$.messages[4].tool_call_id").value("call_2"))
                .andRespond(withSuccess(
                        "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"共两行\"}}]}",
                        MediaType.APPLICATION_JSON));

        Assertions.assertEquals(new FinalAnswer("共两行"), planner.plan("统计两次", history));
        server.verify();
    }

    @Test
    void advertisesRegisteredToolsWithoutPlannerChanges() {
        HarnessTool customTool = new HarnessTool() {
            @Override
            public String getName() { return "demo_tool"; }

            @Override
            public String getDescription() { return "演示工具"; }

            @Override
            public String execute(String input) { return input; }
        };
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        DeepSeekAgentPlanner planner = new DeepSeekAgentPlanner(builder, "test-key",
                new DeepSeekDecisionMapper(), new ToolRegistry(List.of(customTool)));

        server.expect(requestTo("https://api.deepseek.com/chat/completions"))
                .andExpect(jsonPath("$.tools[0].function.name").value("demo_tool"))
                .andExpect(jsonPath("$.tools[0].function.description").value("演示工具"))
                .andRespond(withSuccess(
                        "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"完成\"}}]}",
                        MediaType.APPLICATION_JSON));

        Assertions.assertEquals(new FinalAnswer("完成"), planner.plan("演示", List.of()));
        server.verify();
    }

    @Test
    void mapsFinalAnswerWithoutCallingDeepSeek() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        DeepSeekAgentPlanner planner = new DeepSeekAgentPlanner(builder, "test-key",
                new DeepSeekDecisionMapper(), new ToolRegistry(List.of(new LogErrorCountTool())));

        String responseJson = """
                {"choices":[{"message":{"role":"assistant","content":"模拟回答"}}]}
                """;
        server.expect(requestTo("https://api.deepseek.com/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.tools[0].function.name").value("log_error_count"))
                .andExpect(jsonPath("$.thinking.type").value("disabled"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        AgentDecision decision = planner.plan("你好", List.of());

        FinalAnswer answer = Assertions.assertInstanceOf(FinalAnswer.class, decision);
        Assertions.assertEquals("模拟回答", answer.content());
        server.verify();
    }

    @Test
    void mapsToolCallWithoutCallingDeepSeek() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        DeepSeekAgentPlanner planner = new DeepSeekAgentPlanner(builder, "test-key",
                new DeepSeekDecisionMapper(), new ToolRegistry(List.of(new LogErrorCountTool())));

        String responseJson = """
                {
                  "choices": [{
                    "message": {
                      "role": "assistant",
                      "content": null,
                      "tool_calls": [{
                        "id": "call_1",
                        "type": "function",
                        "function": {
                          "name": "log_error_count",
                          "arguments": "{\\"input\\":\\"ERROR A\\"}"
                        }
                      }]
                    }
                  }]
                }
                """;
        server.expect(requestTo("https://api.deepseek.com/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        AgentDecision decision = planner.plan("统计错误行数", List.of());

        ToolCall toolCall = Assertions.assertInstanceOf(ToolCall.class, decision);
        Assertions.assertEquals("call_1", toolCall.callId());
        Assertions.assertEquals("log_error_count", toolCall.toolName());
        Assertions.assertEquals("ERROR A", toolCall.input());
        server.verify();
    }

    @Test
    void runsToolCallThenReturnsFinalAnswer(){

      RestClient.Builder builder = RestClient.builder();
      MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
      DeepSeekAgentPlanner planner = new DeepSeekAgentPlanner(builder, "test-key",
              new DeepSeekDecisionMapper(), new ToolRegistry(List.of(new LogErrorCountTool())));

      String responseJson1 = """
              {
                "choices": [{
                  "message": {
                    "role": "assistant",
                    "content": null,
                    "tool_calls": [{
                      "id": "call_1",
                      "type": "function",
                      "function": {
                        "name": "log_error_count",
                        "arguments": "{\\"input\\":\\"ERROR A\\"}"
                      }
                    }]
                  }
                }]
              }
              """;
      String responseJson2 = """
              {"choices":[{"message":{"role":"assistant","content":"共有 1 行错误"}}]}
              """;
      server.expect(requestTo("https://api.deepseek.com/chat/completions"))
              .andExpect(method(HttpMethod.POST))
              .andRespond(withSuccess(responseJson1, MediaType.APPLICATION_JSON));
      server.expect(requestTo("https://api.deepseek.com/chat/completions"))
              .andExpect(method(HttpMethod.POST))
              .andExpect(jsonPath("$.messages[2].tool_call_id").value("call_1"))
              .andExpect(jsonPath("$.messages[2].content").value("1"))
              .andRespond(withSuccess(responseJson2, MediaType.APPLICATION_JSON));
      AgentRunner runner = new AgentRunner(planner, new ToolRegistry(List.of(new LogErrorCountTool())));
      String result = runner.run("统计错误行数");
      Assertions.assertEquals("共有 1 行错误",result);

      server.verify();

    }
}
