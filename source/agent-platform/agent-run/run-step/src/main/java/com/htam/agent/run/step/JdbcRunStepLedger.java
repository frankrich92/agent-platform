package com.htam.agent.run.step;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htam.agent.runtime.AgentRunStatus;
import com.htam.agent.runtime.RunStep;
import com.htam.agent.runtime.RunStepType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;

public class JdbcRunStepLedger implements RunStepLedger {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcRunStepLedger(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Override
    public void append(RunStep step) {
        if (step == null) {
            return;
        }
        if (exists(step.stepId())) {
            update(step);
        } else {
            insert(step);
        }
    }

    @Override
    public List<RunStep> listByRunId(String runId) {
        return jdbcTemplate.query(
                "SELECT step_id, run_id, step_type, status, started_at, ended_at, summary_json"
                        + " FROM agent_run_step WHERE run_id = ? ORDER BY started_at, step_id",
                this::mapRow,
                runId);
    }

    private boolean exists(String stepId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM agent_run_step WHERE step_id = ?",
                Integer.class,
                stepId);
        return count != null && count > 0;
    }

    private void insert(RunStep step) {
        jdbcTemplate.update(
                "INSERT INTO agent_run_step"
                        + " (step_id, run_id, step_type, status, started_at, ended_at, summary_json)"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?)",
                step.stepId(),
                step.runId(),
                step.stepType().name(),
                step.status().name(),
                timestamp(step.startedAt()),
                timestamp(step.endedAt()),
                json(step.summary()));
    }

    private void update(RunStep step) {
        jdbcTemplate.update(
                "UPDATE agent_run_step SET run_id = ?, step_type = ?, status = ?, started_at = ?,"
                        + " ended_at = ?, summary_json = ? WHERE step_id = ?",
                step.runId(),
                step.stepType().name(),
                step.status().name(),
                timestamp(step.startedAt()),
                timestamp(step.endedAt()),
                json(step.summary()),
                step.stepId());
    }

    private RunStep mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new RunStep(
                rs.getString("step_id"),
                rs.getString("run_id"),
                RunStepType.valueOf(rs.getString("step_type")),
                AgentRunStatus.valueOf(rs.getString("status")),
                instant(rs.getTimestamp("started_at")),
                instant(rs.getTimestamp("ended_at")),
                map(rs.getString("summary_json")));
    }

    private String json(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Map.of() : value);
        } catch (Exception e) {
            throw new IllegalStateException("运行步骤序列化失败", e);
        }
    }

    private Map<String, Object> map(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (Exception e) {
            return Map.of("raw", json);
        }
    }

    private static Timestamp timestamp(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    private static Instant instant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }
}
