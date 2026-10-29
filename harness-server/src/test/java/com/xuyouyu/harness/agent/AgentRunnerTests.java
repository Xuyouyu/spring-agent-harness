package com.xuyouyu.harness.agent;

import com.xuyouyu.harness.tool.ToolRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;
import com.xuyouyu.harness.tool.LogErrorCountTool;

import org.junit.jupiter.api.Assertions;

public class AgentRunnerTests {
    public class FakePlanner implements AgentPlanner {
        @Override
        public AgentDecision plan(String prompt, List<ToolExecutionResult> history) {
            if (history.isEmpty()) {
                return new ToolCall("1","log_error_count", prompt);
            } else {
                return new FinalAnswer("Final Answer: " + history.getLast().output());
            }
        }
    }
    
    @Test
    public void testAgentRunner() {
        AgentPlanner planner = new FakePlanner();
        ToolRegistry toolRegistry = new ToolRegistry(List.of(new LogErrorCountTool()));
        AgentRunner Runner = new AgentRunner(planner, toolRegistry);
        
        String prompt = "Test prompt\n "+
                    "1. ERROR: NullPointerException at line 42\n" +
                    "2. ERROR: ArrayIndexOutOfBoundsException at line 88";
        String result = Runner.run(prompt);
        
        Assertions.assertEquals("Final Answer: 2", result);
        System.out.println("Result: " + result);
    }

    @Test 
    public void testAgentRunnerWithUnknownTool() {
        ToolRegistry toolRegistry = new ToolRegistry(List.of(new LogErrorCountTool()));
        String prompt = "Test prompt with unknown tool";
        
        // Temporarily override the plan method to return an unknown tool
        AgentPlanner unknownToolPlanner = new AgentPlanner() {
            @Override
            public AgentDecision plan(String prompt, List<ToolExecutionResult> history) {
                if (history.isEmpty()) {
                    return new ToolCall("2","unknown_tool", prompt);
                } else {
                    return new FinalAnswer("Final Answer: " + history.getLast().output());
                }
            }
        };
        
        AgentRunner Runner = new AgentRunner(unknownToolPlanner, toolRegistry);
        
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Runner.run(prompt);
        });
    }

    @Test
    public void testMultipleToolCalls() {
        AgentPlanner planner = (prompt, history) -> {
            if (history.isEmpty()) {
                return new ToolCall("call_1", "log_error_count", "ERROR A");
            }
            if (history.size() == 1) {
                return new ToolCall("call_2", "log_error_count", "ERROR A\nERROR B");
            }
            return new FinalAnswer(history.get(0).output() + "," + history.get(1).output());
        };
        AgentRunner runner = new AgentRunner(planner,
                new ToolRegistry(List.of(new LogErrorCountTool())));

        Assertions.assertEquals("1,2", runner.run("统计两段日志"));
    }

    @Test
    public void testStopsRepeatedToolCalls() {
        AgentPlanner planner = (prompt, history) ->
                new ToolCall("call_" + history.size(), "log_error_count", "ERROR A");
        AgentRunner runner = new AgentRunner(planner,
                new ToolRegistry(List.of(new LogErrorCountTool())));

        IllegalStateException error = Assertions.assertThrows(IllegalStateException.class,
                () -> runner.run("一直调用工具"));
        Assertions.assertTrue(error.getMessage().contains("上限"));
    }
}
