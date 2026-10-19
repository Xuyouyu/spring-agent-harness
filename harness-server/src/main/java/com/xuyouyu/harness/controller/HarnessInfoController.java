package com.xuyouyu.harness.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class HarnessInfoController {
    @GetMapping("/api/harness/info")
    public String info() {
        return "Spring Agent Harness";
    }
}