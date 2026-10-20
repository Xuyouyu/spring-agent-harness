package com.xuyouyu.harness.llm;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;

import java.util.List;

@Service
public class DeepSeekModelClient implements ModelClient {
    private final RestClient restClient;
    private final String apiKey;

    public DeepSeekModelClient(RestClient.Builder builder, @Value("${DEEPSEEK_API_KEY:}") String apiKey) {
        builder.baseUrl("https://api.deepseek.com").defaultHeader("Authorization",
                "Bearer " + apiKey);
        this.restClient = builder.build();
        this.apiKey = apiKey;
    }

    @Override
    public String generate(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("未配置 DeepSeek API Key");
        }
        DeepSeekChatMessage userMessage = new DeepSeekChatMessage("user", prompt);
        List<DeepSeekChatMessage> messages = List.of(userMessage);
        DeepSeekChatRequest request = new DeepSeekChatRequest("deepseek-flash", messages, false);
        DeepSeekChatResponse response = restClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(DeepSeekChatResponse.class);
        return response.choices().get(0).message().content();
    }
}
