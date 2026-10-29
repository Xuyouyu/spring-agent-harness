package com.xuyouyu.harness.llm;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DeepSeekToolResultMessage(String role,@JsonProperty("tool_call_id") String toolCallId,String content) implements DeepSeekRequestMessage{

}
