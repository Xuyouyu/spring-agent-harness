package com.xuyouyu.harness.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.springframework.stereotype.Component;

import com.xuyouyu.harness.tool.ToolRegistry;


@Component
public class AgentRunner {
    private static final int MAX_TOOL_CALLS = 3;
    private final AgentPlanner planner;
    private final ToolRegistry toolRegistry;

    public AgentRunner(AgentPlanner planner, ToolRegistry toolRegistry) {
        this.planner = planner;
        this.toolRegistry = toolRegistry;
    }

    public String run(String prompt) {
        return run(prompt, (type, detail) -> { });
    }

    public String run(String prompt, BiConsumer<String, String> eventSink) {
        List<ToolExecutionResult> history = new ArrayList<>();
        while (true) {
            AgentDecision decision = planner.plan(prompt, List.copyOf(history));
            if (decision instanceof FinalAnswer finalAnswer) {
                return finalAnswer.content();
            }
            if (decision instanceof ToolCall toolCall) {
                if (history.size() >= MAX_TOOL_CALLS) {
                    throw new IllegalStateException("工具调用次数超过上限: " + MAX_TOOL_CALLS);
                }
                eventSink.accept("TOOL_CALL", toolCall.toolName());
                String output = toolRegistry.executeTool(toolCall.toolName(), toolCall.input());
                eventSink.accept("TOOL_RESULT", toolCall.toolName());
                history.add(new ToolExecutionResult(toolCall, output));
                continue;
            }
            throw new IllegalStateException("未知决策类型");
        }
    }
}
