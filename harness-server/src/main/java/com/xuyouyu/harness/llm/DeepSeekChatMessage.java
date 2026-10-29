package com.xuyouyu.harness.llm;

public record DeepSeekChatMessage(String role, String content) implements DeepSeekRequestMessage {

}
