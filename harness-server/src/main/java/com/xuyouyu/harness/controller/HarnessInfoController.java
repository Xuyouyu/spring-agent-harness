package com.xuyouyu.harness.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import com.xuyouyu.harness.dto.HarnessInfo;

@RestController
public class HarnessInfoController {
    @GetMapping("/api/harness/info")
    public HarnessInfo info() {
        return new HarnessInfo("Spring Agent Harness", "0.0.1");
    }
}