package com.xuyouyu.harness.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.xuyouyu.harness.agent.AgentRunner;

@Service
public class TaskExecutionService {
    private static final Logger log = LoggerFactory.getLogger(TaskExecutionService.class);
    private final TaskRunService taskRunService;
    private final AgentRunner agentRunner;

    public TaskExecutionService(TaskRunService taskRunService, AgentRunner agentRunner) {
        this.taskRunService = taskRunService;
        this.agentRunner = agentRunner;
    }

    @Async
    public void execute(String runId) {
        // Mark the task as running
        String prompt = taskRunService.markRunning(runId).prompt();

        try {

            // Run the agent to get the final answer
            String result = agentRunner.run(prompt,
                    (type, detail) -> taskRunService.addEvent(runId, type, detail));

            // Mark the task as succeeded with the result
            taskRunService.markSucceeded(runId, result);
        } catch (Exception e) {
            log.error("Agent run {} failed", runId, e);
            taskRunService.markFailed(runId, "Agent 执行失败，请查看服务日志");
        }

    }
}
