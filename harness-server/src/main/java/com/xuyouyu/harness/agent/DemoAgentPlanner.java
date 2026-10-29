package com.xuyouyu.harness.agent;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("demo")
public class DemoAgentPlanner implements AgentPlanner {
    @Override
    public AgentDecision plan(String prompt, List<ToolExecutionResult> history) {
        if (!history.isEmpty()) {
            return new FinalAnswer("统计完成，包含 ERROR 的行数：" + history.getLast().output());
        }
        if (prompt.contains("ERROR")) {
            return new ToolCall("demo-call", "log_error_count", prompt);
        }
        return new FinalAnswer("演示模式回答：" + prompt);
    }
}
