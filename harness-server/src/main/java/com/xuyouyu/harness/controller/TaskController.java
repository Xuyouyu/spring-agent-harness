package com.xuyouyu.harness.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;
import com.xuyouyu.harness.dto.SubmitTaskRequest;
import com.xuyouyu.harness.dto.SubmitTaskResponse;
import jakarta.validation.Valid;

@RestController
public class TaskController {
    @PostMapping("/api/harness/runs")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public SubmitTaskResponse runs(@RequestBody @Valid SubmitTaskRequest request) {
        return new SubmitTaskResponse("ACCEPTED", "任务已接收");
    }
}
