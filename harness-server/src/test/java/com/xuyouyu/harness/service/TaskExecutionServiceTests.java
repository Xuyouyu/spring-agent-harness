package com.xuyouyu.harness.service;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import com.xuyouyu.harness.model.TaskRun;
import com.xuyouyu.harness.model.RunEvent;
import com.xuyouyu.harness.agent.AgentDecision;
import com.xuyouyu.harness.agent.AgentPlanner;
import com.xuyouyu.harness.agent.AgentRunner;
import com.xuyouyu.harness.agent.FinalAnswer;
import com.xuyouyu.harness.agent.ToolCall;
import com.xuyouyu.harness.agent.ToolExecutionResult;
import com.xuyouyu.harness.tool.LogErrorCountTool;
import com.xuyouyu.harness.tool.ToolRegistry;

public class TaskExecutionServiceTests {
    private static class FakePlanner implements AgentPlanner {
        @Override
        public AgentDecision plan(String prompt, List<ToolExecutionResult> history) {
            if (history.isEmpty()) {
                return new ToolCall("call_1", "log_error_count", prompt);
            }
            return new FinalAnswer("错误行数：" + history.getLast().output());
        }
    }

    private static class MissingToolPlanner implements AgentPlanner {
        @Override
        public AgentDecision plan(String prompt, List<ToolExecutionResult> history) {
            return new ToolCall("call_2", "missing_tool", prompt);
        }
    }
    @Test
    public void testExecute() {
        TaskRunService taskRunService = TaskRunServiceTestFactory.create();
        TaskRun run = taskRunService.createRun("INFO started\nERROR A\nERROR B");
        ToolRegistry toolRegistry = new ToolRegistry(List.of(new LogErrorCountTool()));
        AgentRunner agentRunner = new AgentRunner(new FakePlanner(), toolRegistry);
        TaskExecutionService taskExecutionService = new TaskExecutionService(taskRunService, agentRunner);

        taskExecutionService.execute(run.runId());

        TaskRun finished = taskRunService.findRun(run.runId());
        Assertions.assertEquals("SUCCEEDED", finished.status());
        Assertions.assertEquals("错误行数：2", finished.result());
        Assertions.assertNull(finished.errorMessage());
        Assertions.assertEquals(List.of("ACCEPTED", "RUNNING", "TOOL_CALL", "TOOL_RESULT", "SUCCEEDED"),
                taskRunService.findEvents(run.runId()).stream().map(RunEvent::type).toList());
    }

    @Test
    public void testExecuteFailure() {
        TaskRunService taskRunService = TaskRunServiceTestFactory.create();
        TaskRun run = taskRunService.createRun("测试任务");
        ToolRegistry toolRegistry = new ToolRegistry(List.of(new LogErrorCountTool()));
        AgentRunner agentRunner = new AgentRunner(new MissingToolPlanner(), toolRegistry);
        TaskExecutionService taskExecutionService = new TaskExecutionService(taskRunService, agentRunner);

        taskExecutionService.execute(run.runId());

        TaskRun finished = taskRunService.findRun(run.runId());
        Assertions.assertEquals("FAILED", finished.status());
        Assertions.assertEquals("Agent 执行失败，请查看服务日志", finished.errorMessage());
        Assertions.assertNull(finished.result());
        Assertions.assertEquals(List.of("ACCEPTED", "RUNNING", "TOOL_CALL", "FAILED"),
                taskRunService.findEvents(run.runId()).stream().map(RunEvent::type).toList());
    }
}
