package com.xuyouyu.harness.service;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.xuyouyu.harness.model.TaskRun;

class TaskRunServicePersistenceTests {
    @TempDir
    Path tempDir;

    @Test
    void keepsRunsAcrossServiceInstancesAndRecoversInterruptedWork() {
        String databaseUrl = "jdbc:h2:file:"
                + tempDir.resolve("runs").toString().replace('\\', '/');
        TaskRunService first = TaskRunServiceTestFactory.create(databaseUrl);
        TaskRun interrupted = first.createRun("ERROR A");
        first.markRunning(interrupted.runId());
        TaskRun completed = first.createRun("已完成任务");
        first.markRunning(completed.runId());
        first.markSucceeded(completed.runId(), "完成");

        TaskRunService restarted = TaskRunServiceTestFactory.create(databaseUrl);
        Assertions.assertEquals("RUNNING", restarted.findRun(interrupted.runId()).status());
        Assertions.assertEquals("SUCCEEDED", restarted.findRun(completed.runId()).status());
        Assertions.assertEquals(1, restarted.recoverUnfinishedRuns(Instant.now().plusSeconds(1)));

        TaskRunService reopened = TaskRunServiceTestFactory.create(databaseUrl);
        Assertions.assertEquals("FAILED", reopened.findRun(interrupted.runId()).status());
        Assertions.assertEquals("服务重启中断任务", reopened.findRun(interrupted.runId()).errorMessage());
        Assertions.assertEquals("SUCCEEDED", reopened.findRun(completed.runId()).status());
        Assertions.assertEquals(List.of("ACCEPTED", "RUNNING", "FAILED"),
                reopened.findEvents(interrupted.runId()).stream().map(event -> event.type()).toList());
        Assertions.assertEquals(0, reopened.recoverUnfinishedRuns(Instant.now().plusSeconds(1)));
    }
}
