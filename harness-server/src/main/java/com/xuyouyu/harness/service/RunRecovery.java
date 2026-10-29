package com.xuyouyu.harness.service;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class RunRecovery {
    private static final Logger log = LoggerFactory.getLogger(RunRecovery.class);

    private final Instant startedAt = Instant.now();
    private final TaskRunService taskRunService;

    public RunRecovery(TaskRunService taskRunService) {
        this.taskRunService = taskRunService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void recover() {
        int recovered = taskRunService.recoverUnfinishedRuns(startedAt);
        if (recovered > 0) {
            log.warn("Recovered {} interrupted Agent runs", recovered);
        }
    }
}
