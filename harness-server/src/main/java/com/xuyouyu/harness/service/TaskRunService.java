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
        TaskRun run = new TaskRun(runId, prompt, "ACCEPTED");
        runs.put(runId, run);
        return run;
    }

    public TaskRun findRun(String runId) {
        return runs.get(runId);
    }

}
