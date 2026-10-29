package com.xuyouyu.harness.tool;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component 
public class ToolRegistry {
    private final Map<String,HarnessTool> tools = new ConcurrentHashMap<>();

    public ToolRegistry(List<HarnessTool> toolList) {
        for (HarnessTool tool : toolList) {
            register(tool);
        }
    }
    public void register(HarnessTool tool) {
        HarnessTool previous = tools.putIfAbsent(tool.getName(), tool);
        if (previous != null) {
            throw new IllegalArgumentException("重复的工具名称: " + tool.getName());
        }
    }

    public List<HarnessTool> listTools() {
        return tools.values().stream()
                .sorted((left, right) -> left.getName().compareTo(right.getName()))
                .toList();
    }
    
    public String executeTool(String toolName, String input) {
        HarnessTool tool = tools.get(toolName);
        if (tool == null) {
            throw new IllegalArgumentException("未找到该工具: " + toolName);
        }
        return tool.execute(input);
    }
    
}
