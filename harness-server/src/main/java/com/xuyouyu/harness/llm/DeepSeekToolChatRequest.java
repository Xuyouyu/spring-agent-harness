package com.xuyouyu.harness.llm;

import java.util.List;

public record DeepSeekToolChatRequest(String model, List<DeepSeekChatMessage> messages, boolean stream,List<DeepSeekToolDefinition> tools) {
    
}
