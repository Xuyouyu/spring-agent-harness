package com.xuyouyu.harness.tool;

public interface HarnessTool {
    String getName();
    String getDescription();
    String execute(String input);
}