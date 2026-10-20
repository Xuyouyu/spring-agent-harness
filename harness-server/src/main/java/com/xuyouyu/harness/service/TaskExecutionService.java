package com.xuyouyu.harness.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.xuyouyu.harness.llm.ModelClient;

@Service
public class TaskExecutionService {
    private final TaskRunService taskRunService;
    private final ModelClient modelClient;

    public TaskExecutionService(TaskRunService taskRunService, ModelClient modelClient) {
        this.taskRunService = taskRunService;
        this.modelClient = modelClient;
    }

    @Async
    public void execute(String runId) {
        // Mark the task as running
        String prompt = taskRunService.markRunning(runId).prompt();

        try {

            // Generate the result using the model client
            String result = modelClient.generate(prompt);

            // Mark the task as succeeded with the result
            taskRunService.markSucceeded(runId, result);
        } catch (Exception e) {
            // If any exception occurs, mark the task as failed with the error message
            taskRunService.markFailed(runId, "模型调用失败");
        }

    }
}
