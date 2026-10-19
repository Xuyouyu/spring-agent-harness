package com.xuyouyu.harness.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;
import com.xuyouyu.harness.dto.SubmitTaskRequest;
import com.xuyouyu.harness.dto.SubmitTaskResponse;
import com.xuyouyu.harness.dto.TaskRunStatusResponse;
import com.xuyouyu.harness.model.TaskRun;

import jakarta.validation.Valid;
import com.xuyouyu.harness.service.TaskRunService;

@RestController
public class TaskController {
    private final TaskRunService taskRunService;

    public TaskController(TaskRunService taskRunService) {
        this.taskRunService = taskRunService;
    }

    @PostMapping("/api/harness/runs")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public SubmitTaskResponse runs(@RequestBody @Valid SubmitTaskRequest request) {
        TaskRun run = taskRunService.createRun(request.prompt());
        return new SubmitTaskResponse(run.runId(), run.status(), "任务已接收");
    }

    @GetMapping("/api/harness/runs/{runId}")
    public TaskRunStatusResponse getRun(@PathVariable String runId) {
        TaskRun run = taskRunService.findRun(runId);
        if (run == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Run not found");
        } else {
            return new TaskRunStatusResponse(run.runId(), run.status());
        }
    }

}
