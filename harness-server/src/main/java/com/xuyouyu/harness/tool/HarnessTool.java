package com.xuyouyu.harness.tool;

import java.util.List;
import java.util.Map;

public interface HarnessTool {
    String getName();
    String getDescription();
    String execute(String input);

    default Map<String, Object> getParameters() {
        return Map.of(
                "type", "object",
                "properties", Map.of("input", Map.of("type", "string")),
                "required", List.of("input"));
    }
}
