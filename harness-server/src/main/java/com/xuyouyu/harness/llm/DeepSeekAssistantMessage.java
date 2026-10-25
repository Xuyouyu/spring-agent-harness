package com.xuyouyu.harness.llm;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DeepSeekAssistantMessage(String role, String content,@JsonProperty("tool_calls") List<DeepSeekToolCall> toolCalls) {
    
}
