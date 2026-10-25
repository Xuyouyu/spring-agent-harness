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
        List<DeepSeekChatMessage> messages = List.of(new  DeepSeekChatMessage("user","统计这段日志中的错误行数"));
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
}
