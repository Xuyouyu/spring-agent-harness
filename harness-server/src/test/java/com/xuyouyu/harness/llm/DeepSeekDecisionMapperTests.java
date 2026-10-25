package com.xuyouyu.harness.llm;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.xuyouyu.harness.agent.AgentDecision;
import com.xuyouyu.harness.agent.FinalAnswer;
import com.xuyouyu.harness.agent.ToolCall;

public class DeepSeekDecisionMapperTests {
    @Test 
    public void testNormalResponse(){
        DeepSeekAssistantMessage message = new DeepSeekAssistantMessage("assistant", "你好", null);
        DeepSeekChatChoice choice = new DeepSeekChatChoice(message);
        DeepSeekChatResponse response = new DeepSeekChatResponse(List.of(choice));
        DeepSeekDecisionMapper decisionMapper = new DeepSeekDecisionMapper();
        AgentDecision actual = decisionMapper.map(response);
        Assertions.assertEquals(new FinalAnswer("你好"),actual);
    }

    @Test
    public void testToolCall(){
        DeepSeekFunctionCall functionCall = new DeepSeekFunctionCall("log_error_count","{\"input\":\"ERROR A\"}");
        DeepSeekToolCall toolCall = new  DeepSeekToolCall("call_1","function",functionCall);
        DeepSeekAssistantMessage message = new DeepSeekAssistantMessage("assistant", null,List.of(toolCall));
        DeepSeekChatChoice choice = new DeepSeekChatChoice(message);
        DeepSeekChatResponse response = new DeepSeekChatResponse(List.of(choice));
        DeepSeekDecisionMapper decisionMapper = new DeepSeekDecisionMapper();
        AgentDecision actual = decisionMapper.map(response);
        Assertions.assertEquals(new ToolCall("call_1", "log_error_count", "ERROR A"),actual);

    }

    @Test 
    public void testNochoice(){
         DeepSeekChatResponse response = new DeepSeekChatResponse(List.of());
         DeepSeekDecisionMapper decisionMapper = new DeepSeekDecisionMapper();
         Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> decisionMapper.map(response));
    }

    @Test 
    public void testNoinpuut(){
        DeepSeekFunctionCall functionCall = new DeepSeekFunctionCall("log_error_count","{}");
        DeepSeekToolCall toolCall = new  DeepSeekToolCall("call_1","function",functionCall);
        DeepSeekAssistantMessage message = new DeepSeekAssistantMessage("assistant", null,List.of(toolCall));
        DeepSeekChatChoice choice = new DeepSeekChatChoice(message);
        DeepSeekChatResponse response = new DeepSeekChatResponse(List.of(choice));
        DeepSeekDecisionMapper decisionMapper = new DeepSeekDecisionMapper();
        Assertions.assertThrows(
            IllegalArgumentException.class, 
            () -> decisionMapper.map(response));
    }

}
