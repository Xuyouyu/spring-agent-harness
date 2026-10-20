package com.xuyouyu.harness.llm;

import java.util.List;

public record DeepSeekChatRequest(String model, List<DeepSeekChatMessage> messages, boolean stream) {

}
