package com.xuyouyu.harness.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import com.xuyouyu.harness.model.TaskRun;
import com.xuyouyu.harness.llm.ModelClient;

public class TaskExecutionServiceTests {
    // This class is intended for testing the TaskExecutionService.
    public class TestModelClient implements ModelClient {
        @Override
        public String generate(String prompt) {
            // Simulate a successful model generation
            return "端口测试成功";
        }
    }

    public class TestModelClientFailure implements ModelClient {
        @Override
        public String generate(String prompt) {
            // Simulate a failure in model generation
            throw new RuntimeException("模型调用失败");
        }
    }
    @Test 
    public void testExecute() {
        TaskRunService taskRunService = new TaskRunService();
        TaskRun run = taskRunService.createRun("测试任务");
        TestModelClient testModelClient = new TestModelClient();
        TaskExecutionService taskExecutionService = new TaskExecutionService(taskRunService, testModelClient);
        taskExecutionService.execute(run.runId());
        TaskRun finished=taskRunService.findRun(run.runId());
        Assertions.assertEquals("SUCCEEDED", finished.status());
        Assertions.assertEquals("端口测试成功", finished.result());
    }

    @Test 
    public void testExecuteFailure() {
        TaskRunService taskRunService = new TaskRunService();
        TaskRun run = taskRunService.createRun("测试任务");
        TestModelClientFailure testModelClientFailure = new TestModelClientFailure();
        TaskExecutionService taskExecutionService = new TaskExecutionService(taskRunService, testModelClientFailure);
        taskExecutionService.execute(run.runId());
        TaskRun finished=taskRunService.findRun(run.runId());
        Assertions.assertEquals("FAILED", finished.status());
        Assertions.assertEquals("模型调用失败", finished.errorMessage());
        Assertions.assertNull(finished.result());
    }

}