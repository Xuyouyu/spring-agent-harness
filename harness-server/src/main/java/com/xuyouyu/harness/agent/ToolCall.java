package com.xuyouyu.harness.agent;

public record ToolCall(String toolName, String input) implements AgentDecision {
    
}
