package com.xuyouyu.harness.llm;


import org.springframework.stereotype.Component;

import com.xuyouyu.harness.agent.AgentDecision;
import com.xuyouyu.harness.agent.FinalAnswer;
import com.xuyouyu.harness.agent.ToolCall;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Component
public class DeepSeekDecisionMapper {
    public AgentDecision map(DeepSeekChatResponse deepSeekChatResponse) {
        // Implementation for mapping DeepSeekChatResponse to AgentDecision
        if (deepSeekChatResponse == null || deepSeekChatResponse.choices() == null || deepSeekChatResponse.choices().isEmpty()) {
            throw new IllegalArgumentException("模型响应为空或缺少 choices");
        }
        if (deepSeekChatResponse.choices().get(0) != null && deepSeekChatResponse.choices().get(0).message() != null) {
            DeepSeekAssistantMessage message =  deepSeekChatResponse.choices().get(0).message();
            if ( message.toolCalls() != null && !message.toolCalls().isEmpty()) {
                if (message.toolCalls().size() != 1) {
                    throw new IllegalArgumentException("暂不支持同时调用多个工具");
                }
                DeepSeekToolCall toolCall = message.toolCalls().get(0);

                if (toolCall == null) {
                    throw new IllegalArgumentException("工具调用为空");
                }
                if (toolCall.id() == null || toolCall.id().isBlank()) {
                    throw new IllegalArgumentException("工具调用 ID 为空");
                }
                if (!"function".equals(toolCall.type())) {
                    throw new IllegalArgumentException("不支持的工具调用类型");
                }
                DeepSeekFunctionCall function = toolCall.function();
                if (function == null) {
                    throw new IllegalArgumentException("缺少工具函数信息");
                }
                if (function.name() == null || function.name().isBlank()) {
                    throw new IllegalArgumentException("工具名称为空");
                }
                if (function.arguments() == null || function.arguments().isBlank()) {
                    throw new IllegalArgumentException("工具参数为空");
                }
                DeepSeekToolArguments arguments;
                try{
                    arguments= new  JsonMapper().readValue(
                        function.arguments(),DeepSeekToolArguments.class);
                }catch (JacksonException e){
                    throw new IllegalArgumentException("工具参数不是合法json",e);
                }
                if (arguments == null || arguments.input() == null || arguments.input().isBlank()) {
                    throw new IllegalArgumentException("工具参数缺少 input");
                }

                return new ToolCall(toolCall.id(), function.name(), arguments.input());
            }
            if (message.content() != null
                 && !message.content().isBlank()
                 && (message.toolCalls() == null || message.toolCalls().isEmpty())) {
                    return new FinalAnswer(message.content());
                }
        }
        throw new IllegalArgumentException("无法映射模型响应到代理决策");
    }
}
