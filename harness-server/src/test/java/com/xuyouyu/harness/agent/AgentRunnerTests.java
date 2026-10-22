package com.xuyouyu.harness.agent;

import com.xuyouyu.harness.tool.ToolRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;
import com.xuyouyu.harness.tool.LogErrorCountTool;

import org.junit.jupiter.api.Assertions;

public class AgentRunnerTests {
    public class FakePlanner implements AgentPlanner {
        @Override
        public AgentDecision plan(String prompt, String toolresult) {
            if (toolresult == null) {
                return new ToolCall("log_error_count", prompt);
            } else {
                return new FinalAnswer("Final Answer: " + toolresult);
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
            public AgentDecision plan(String prompt, String toolresult) {
                if (toolresult == null) {
                    return new ToolCall("unknown_tool", prompt);
                } else {
                    return new FinalAnswer("Final Answer: " + toolresult);
                }
            }
        };
        
        AgentRunner Runner = new AgentRunner(unknownToolPlanner, toolRegistry);
        
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Runner.run(prompt);
        });
    }
}
