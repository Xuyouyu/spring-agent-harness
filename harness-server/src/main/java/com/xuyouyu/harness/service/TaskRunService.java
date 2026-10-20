package com.xuyouyu.harness.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.xuyouyu.harness.model.TaskRun;

@Service
public class TaskRunService {
    private final Map<String, TaskRun> runs = new ConcurrentHashMap<>();

    public TaskRun createRun(String prompt) {
        String runId = UUID.randomUUID().toString();
        TaskRun run = new TaskRun(runId, prompt, "ACCEPTED", null, null);
        runs.put(runId, run);
        return run;
    }

    public TaskRun findRun(String runId) {
        return runs.get(runId);
    }

    public TaskRun markRunning(String runId) {
        TaskRun run = runs.get(runId);
        if (run != null) {
            if (!"ACCEPTED".equals(run.status())) {
                throw new IllegalStateException("当前状态不可执行该操作");
            }
            TaskRun updatedRun = new TaskRun(run.runId(), run.prompt(), "RUNNING", null, null);
            runs.put(runId, updatedRun);
            return updatedRun;
        }
        throw new IllegalArgumentException("未找到该任务");
    }

    public TaskRun markSucceeded(String runId, String result) {
        TaskRun run = runs.get(runId);
        if (run != null) {
            if (!"RUNNING".equals(run.status())) {
                throw new IllegalStateException("当前状态不可执行该操作");
            }
            TaskRun updatedRun = new TaskRun(run.runId(), run.prompt(), "SUCCEEDED", result, null);
            runs.put(runId, updatedRun);
            return updatedRun;
        }
        throw new IllegalArgumentException("未找到该任务");
    }

    public TaskRun markFailed(String runId, String errorMessage) {
        TaskRun run = runs.get(runId);
        if (run != null) {
            if (!"RUNNING".equals(run.status())) {
                throw new IllegalStateException("当前状态不可执行该操作");
            }
            TaskRun updatedRun = new TaskRun(run.runId(), run.prompt(), "FAILED", null, errorMessage);
            runs.put(runId, updatedRun);
            return updatedRun;
        }
        throw new IllegalArgumentException("未找到该任务");
    }

}
