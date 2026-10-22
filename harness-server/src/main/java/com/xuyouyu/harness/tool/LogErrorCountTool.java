package com.xuyouyu.harness.tool;

import org.springframework.stereotype.Component;

@Component 
public class LogErrorCountTool implements HarnessTool {
    @Override
    public String getName() {
        return "log_error_count";
    }

    @Override
    public String getDescription() {
        return "统计日志中包含错误信息的数量";
    }

    @Override
    public String execute(String input) {
        // 统计日志中包含错误信息的数量
        int errorCount = 0;
        String[] lines = input.split("\n");
        for (String line : lines) {
            if (line.contains("ERROR")) {
                errorCount++;
            }
        }
        return String.valueOf(errorCount);
    }
    
}
