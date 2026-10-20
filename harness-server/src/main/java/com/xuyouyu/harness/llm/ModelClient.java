package com.xuyouyu.harness.llm;

public interface ModelClient {

    public abstract String generate(String prompt);
}