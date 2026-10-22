package com.xuyouyu.harness.tool;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class ToolRegistryTests {
    private ToolRegistry toolRegistry;
    @Test 
    public void testLogErrorCountTool() {
        LogErrorCountTool logErrorCountTool = new LogErrorCountTool();
        toolRegistry = new ToolRegistry(java.util.List.of(logErrorCountTool));
        String input = "INFO started\n" + //
                        "ERROR A ERROR B\n" + //
                        "ERROR C";
        String result = toolRegistry.executeTool("log_error_count", input);
        Assertions.assertEquals("2", result);
    }

    @Test 
    public void testToolNotFound() {
        toolRegistry = new ToolRegistry(java.util.List.of());
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            toolRegistry.executeTool("missing_tool", "some input");
        });
    }
}
