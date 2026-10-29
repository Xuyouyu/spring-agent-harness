package com.xuyouyu.harness.llm;

import java.util.List;
import java.util.Map;

public record DeepSeekToolChatRequest(
        String model,
        List<DeepSeekRequestMessage> messages,
        boolean stream,
        List<DeepSeekToolDefinition> tools,
        Map<String, String> thinking) {

    public DeepSeekToolChatRequest(String model, List<DeepSeekRequestMessage> messages,
            boolean stream, List<DeepSeekToolDefinition> tools) {
        this(model, messages, stream, tools, Map.of("type", "disabled"));
    }
}
