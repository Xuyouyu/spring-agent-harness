package com.xuyouyu.harness.llm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import tools.jackson.databind.json.JsonMapper;

public class DeepSeekChatResponseTests {
    @Test 
    public void testJsonDeserialization() {
        String json = "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"你好\"}}]}";
        JsonMapper mapper = new JsonMapper();
        DeepSeekChatResponse response = mapper.readValue(json, DeepSeekChatResponse.class);
        Assertions.assertEquals("assistant", response.choices().get(0).message().role());
        Assertions.assertEquals("你好", response.choices().get(0).message().content());
    }

    @Test 
    public void testJsonDeserializationWithToolCalls() {
        String json = "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":null,\"tool_calls\":[{\"id\":\"call_1\",\"type\":\"function\",\"function\":{\"name\":\"log_error_count\",\"arguments\":\"{}\"}}]}}]}";
        JsonMapper mapper = new JsonMapper();
        DeepSeekChatResponse response = mapper.readValue(json, DeepSeekChatResponse.class);
        Assertions.assertEquals("assistant", response.choices().get(0).message().role());
        Assertions.assertEquals(null, response.choices().get(0).message().content());
        Assertions.assertEquals(1, response.choices().get(0).message().toolCalls().size());
        Assertions.assertEquals("call_1", response.choices().get(0).message().toolCalls().get(0).id());
        Assertions.assertEquals("function", response.choices().get(0).message().toolCalls().get(0).type());
        Assertions.assertEquals("log_error_count", response.choices().get(0).message().toolCalls().get(0).function().name());
        Assertions.assertEquals("{}", response.choices().get(0).message().toolCalls().get(0).function().arguments());
    }
}
