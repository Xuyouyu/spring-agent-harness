package com.xuyouyu.harness.agent;

import java.util.List;

public interface AgentPlanner {
    AgentDecision plan(String prompt, List<ToolExecutionResult> history);
}
