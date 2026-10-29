package com.xuyouyu.harness.llm;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.xuyouyu.harness.agent.AgentDecision;
import com.xuyouyu.harness.agent.AgentPlanner;
import com.xuyouyu.harness.agent.ToolExecutionResult;
import com.xuyouyu.harness.tool.HarnessTool;
import com.xuyouyu.harness.tool.ToolRegistry;

import tools.jackson.databind.json.JsonMapper;

@Service
@Profile("!demo")
public class DeepSeekAgentPlanner implements AgentPlanner {
    private final RestClient restClient;
    private final String apiKey;
    private final DeepSeekDecisionMapper mapper;
    private final ToolRegistry toolRegistry;

    public DeepSeekAgentPlanner(RestClient.Builder builder,
            @Value("${DEEPSEEK_API_KEY:}") String apiKey,
            DeepSeekDecisionMapper mapper, ToolRegistry toolRegistry) {
        builder.baseUrl("https://api.deepseek.com").defaultHeader("Authorization",
                "Bearer " + apiKey);
        this.restClient = builder.build();
        this.apiKey = apiKey;
        this.mapper = mapper;
        this.toolRegistry = toolRegistry;
    }

    @Override
    public AgentDecision plan(String prompt, List<ToolExecutionResult> history) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("apiKey为空");
        }
        List<DeepSeekRequestMessage> messages = new ArrayList<>();
        messages.add(new DeepSeekChatMessage("user", prompt));
        JsonMapper jsonMapper = new JsonMapper();
        for (ToolExecutionResult result : history) {
            String argumentsJson = jsonMapper.writeValueAsString(
                new DeepSeekToolArguments(result.toolCall().input())
            );
            DeepSeekToolCall toolCall = new DeepSeekToolCall(
            result.toolCall().callId(),
            "function",
            new DeepSeekFunctionCall(result.toolCall().toolName(), argumentsJson)
            );
            messages.add(new DeepSeekAssistantMessage("assistant", null, List.of(toolCall)));
            messages.add(new DeepSeekToolResultMessage("tool", result.toolCall().callId(), result.output()));
        }
        List<DeepSeekToolDefinition> tools = toolRegistry.listTools().stream()
                .map(this::toDefinition)
                .toList();
        DeepSeekToolChatRequest toolChatRequest = new DeepSeekToolChatRequest("deepseek-flash",messages,false,tools);
        DeepSeekChatResponse response = restClient.post()
        .uri("/chat/completions")
        .contentType(MediaType.APPLICATION_JSON)
        .body(toolChatRequest)
        .retrieve()
        .body(DeepSeekChatResponse.class);
        return mapper.map(response);
    }

    private DeepSeekToolDefinition toDefinition(HarnessTool tool) {
        DeepSeekFunctionDefinition function = new DeepSeekFunctionDefinition(
                tool.getName(), tool.getDescription(), tool.getParameters());
        return new DeepSeekToolDefinition("function", function);
    }
}
