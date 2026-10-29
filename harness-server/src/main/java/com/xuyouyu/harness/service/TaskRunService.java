package com.xuyouyu.harness.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xuyouyu.harness.model.RunEvent;
import com.xuyouyu.harness.model.TaskRun;

@Service
public class TaskRunService {
    private final JdbcTemplate jdbcTemplate;

    public TaskRunService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public TaskRun createRun(String prompt) {
        String runId = UUID.randomUUID().toString();
        TaskRun run = new TaskRun(runId, prompt, "ACCEPTED", null, null);
        jdbcTemplate.update(
                "INSERT INTO task_runs (run_id, prompt, created_at, status, result, error_message) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                runId, prompt, java.sql.Timestamp.from(Instant.now()), run.status(), null, null);
        addEvent(runId, "ACCEPTED", "任务已接收");
        return run;
    }

    @Transactional
    public void addEvent(String runId, String type, String detail) {
        if (findRun(runId) == null) {
            throw new IllegalArgumentException("未找到该任务");
        }
        jdbcTemplate.update(
                "INSERT INTO run_events (run_id, happened_at, type, detail) VALUES (?, ?, ?, ?)",
                runId, java.sql.Timestamp.from(Instant.now()), type, detail);
    }

    public List<RunEvent> findEvents(String runId) {
        if (findRun(runId) == null) {
            throw new IllegalArgumentException("未找到该任务");
        }
        return jdbcTemplate.query(
                "SELECT happened_at, type, detail FROM run_events WHERE run_id = ? ORDER BY id",
                (rs, rowNum) -> new RunEvent(
                        rs.getTimestamp("happened_at").toInstant(),
                        rs.getString("type"), rs.getString("detail")),
                runId);
    }

    public TaskRun findRun(String runId) {
        return jdbcTemplate.query(
                "SELECT run_id, prompt, status, result, error_message FROM task_runs WHERE run_id = ?",
                (rs, rowNum) -> new TaskRun(
                        rs.getString("run_id"), rs.getString("prompt"),
                        rs.getString("status"), rs.getString("result"),
                        rs.getString("error_message")),
                runId).stream().findFirst().orElse(null);
    }

    @Transactional
    public TaskRun markRunning(String runId) {
        return transition(runId, "ACCEPTED", "RUNNING", null, null, "开始执行 Agent");
    }

    @Transactional
    public TaskRun markSucceeded(String runId, String result) {
        return transition(runId, "RUNNING", "SUCCEEDED", result, null, "任务已完成");
    }

    @Transactional
    public TaskRun markFailed(String runId, String errorMessage) {
        return transition(runId, "RUNNING", "FAILED", null, errorMessage, errorMessage);
    }

    @Transactional
    public int recoverUnfinishedRuns(Instant startedAt) {
        List<String> runIds = jdbcTemplate.queryForList(
                "SELECT run_id FROM task_runs WHERE status IN ('ACCEPTED', 'RUNNING') "
                        + "AND created_at < ?",
                String.class, java.sql.Timestamp.from(startedAt));
        int recovered = 0;
        for (String runId : runIds) {
            int updated = jdbcTemplate.update(
                    "UPDATE task_runs SET status = 'FAILED', result = NULL, error_message = ? "
                            + "WHERE run_id = ? AND status IN ('ACCEPTED', 'RUNNING')",
                    "服务重启中断任务", runId);
            if (updated == 1) {
                addEvent(runId, "FAILED", "服务重启中断任务");
                recovered++;
            }
        }
        return recovered;
    }

    private TaskRun transition(String runId, String from, String to,
            String result, String errorMessage, String eventDetail) {
        int updated = jdbcTemplate.update(
                "UPDATE task_runs SET status = ?, result = ?, error_message = ? "
                        + "WHERE run_id = ? AND status = ?",
                to, result, errorMessage, runId, from);
        if (updated == 0) {
            if (findRun(runId) == null) {
                throw new IllegalArgumentException("未找到该任务");
            }
            throw new IllegalStateException("当前状态不可执行该操作");
        }
        addEvent(runId, to, eventDetail);
        return findRun(runId);
    }
}
