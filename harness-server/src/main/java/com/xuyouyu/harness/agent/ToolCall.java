package com.xuyouyu.harness.agent;

public record ToolCall(String callId,String toolName, String input) implements AgentDecision {
    
}
