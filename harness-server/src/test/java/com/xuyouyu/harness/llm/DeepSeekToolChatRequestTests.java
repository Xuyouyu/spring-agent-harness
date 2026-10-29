package com.xuyouyu.harness.llm;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

public class DeepSeekToolChatRequestTests {
    @Test 
    public void testparameters(){
        Map<String,Object> parameters = Map.of(
            "type","object",
            "properties",Map.of(
                "input",Map.of("type","string")
            ),
            "required",List.of("input")
        );
        DeepSeekFunctionDefinition function = new DeepSeekFunctionDefinition(
        "log_error_count",
        "统计日志中包含 ERROR 的行数",
        parameters);
        DeepSeekToolDefinition toolDefinition = new DeepSeekToolDefinition("function",function);
        List<DeepSeekToolDefinition> tools= List.of(toolDefinition);
        List<DeepSeekRequestMessage> messages = List.of(new  DeepSeekChatMessage("user","统计这段日志中的错误行数"));
        DeepSeekToolChatRequest toolChatRequest = new DeepSeekToolChatRequest("deepseek-flash",messages,false,tools);
        JsonMapper mapper = new JsonMapper();
        String json = mapper.writeValueAsString(toolChatRequest);
        JsonNode root = mapper.readTree(json);
        Assertions.assertEquals(
            "string",
             root.path("tools")
                 .get(0)
                 .path("function")
                 .path("parameters")
                 .path("properties")
                 .path("input")
                 .path("type")
                 .stringValue()
        );
        Assertions.assertEquals(
            "统计这段日志中的错误行数",
            root.path("messages").get(0).path("content").stringValue()
        );
        Assertions.assertEquals("disabled", root.path("thinking").path("type").stringValue());

        Assertions.assertEquals(
            "log_error_count",
            root.path("tools").get(0).path("function").path("name").stringValue()
        );

        Assertions.assertEquals(
            "input",
            root.path("tools").get(0).path("function")
                .path("parameters").path("required").get(0).stringValue()
        );
    }

    @Test
    public void testToolResultConversationSerialization() {
        DeepSeekToolCall toolCall = new DeepSeekToolCall(
            "call_1",
            "function",
            new DeepSeekFunctionCall("log_error_count", "{\"input\":\"ERROR A\"}")
        );
        List<DeepSeekRequestMessage> messages = List.of(
            new DeepSeekChatMessage("user", "统计日志中的错误行数"),
            new DeepSeekAssistantMessage("assistant", null, List.of(toolCall)),
            new DeepSeekToolResultMessage("tool", "call_1", "1")
        );
        DeepSeekToolChatRequest request = new DeepSeekToolChatRequest(
            "deepseek-flash", messages, false, List.of()
        );

        JsonMapper mapper = new JsonMapper();
        JsonNode root = mapper.readTree(mapper.writeValueAsString(request));
        JsonNode serializedMessages = root.path("messages");

        Assertions.assertEquals(3, serializedMessages.size());
        Assertions.assertEquals("user", serializedMessages.get(0).path("role").stringValue());
        Assertions.assertEquals("统计日志中的错误行数", serializedMessages.get(0).path("content").stringValue());
        Assertions.assertEquals("assistant", serializedMessages.get(1).path("role").stringValue());
        Assertions.assertEquals("call_1", serializedMessages.get(1).path("tool_calls").get(0).path("id").stringValue());
        Assertions.assertEquals("log_error_count", serializedMessages.get(1).path("tool_calls").get(0)
            .path("function").path("name").stringValue());
        Assertions.assertEquals("{\"input\":\"ERROR A\"}", serializedMessages.get(1).path("tool_calls").get(0)
            .path("function").path("arguments").stringValue());
        Assertions.assertEquals("tool", serializedMessages.get(2).path("role").stringValue());
        Assertions.assertEquals("call_1", serializedMessages.get(2).path("tool_call_id").stringValue());
        Assertions.assertEquals("1", serializedMessages.get(2).path("content").stringValue());
    }
}
