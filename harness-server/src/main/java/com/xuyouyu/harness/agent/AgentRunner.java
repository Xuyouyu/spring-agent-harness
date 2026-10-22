package com.xuyouyu.harness.agent;

import com.xuyouyu.harness.tool.ToolRegistry;



public class AgentRunner {
    private final AgentPlanner planner;
    private final ToolRegistry toolRegistry;

    public AgentRunner(AgentPlanner planner, ToolRegistry toolRegistry) {
        this.planner = planner;
        this.toolRegistry = toolRegistry;
    }

    public String run(String prompt) {
        AgentDecision decision = planner.plan(prompt,null);
        if (decision instanceof FinalAnswer finalAnswer) {
            return finalAnswer.content();
        } else if (decision instanceof ToolCall toolCall) {
            String result = toolRegistry.executeTool(toolCall.toolName(), toolCall.input());
            AgentDecision decision2 = planner.plan(prompt, result);
            if (decision2 instanceof FinalAnswer finalAnswer2) {
                return finalAnswer2.content();
            }else {
                throw new IllegalStateException("计划器未返回最终答案");
            }
        }else {
            throw new IllegalStateException("未知决策类型");
        }
    }
    
}
