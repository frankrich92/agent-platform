package com.htam.agent.run.event;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htam.agent.runtime.RuntimeEvent;
import com.htam.agent.runtime.RuntimeEventType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "agent.run.ledger.event-store", havingValue = "jdbc", matchIfMissing = true)
public class JdbcRuntimeEventLedger implements RuntimeEventLedger {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcRuntimeEventLedger(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Override
    public void append(RuntimeEvent event) {
        if (event == null) {
            return;
        }
        if (exists(event.eventId()) || existsRunSequence(event.runId(), event.sequence())) {
            return;
        }
        try {
            jdbcTemplate.update(
                    "INSERT INTO agent_run_event"
                            + " (event_id, run_id, session_id, step_id, trace_id, sequence, event_type,"
                            + " occurred_at, payload_json)"
                            + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    event.eventId(),
                    event.runId(),
                    event.sessionId(),
                    event.stepId(),
                    event.traceId(),
                    event.sequence(),
                    event.eventType().name(),
                    timestamp(event.timestamp()),
                    json(event.payload()));
        } catch (DuplicateKeyException ignored) {
            // Idempotent replay/retry: the same event or sequence has already been persisted.
        }
    }

    @Override
    public List<RuntimeEvent> listByRunId(String runId) {
        return jdbcTemplate.query(
                "SELECT event_id, event_type, run_id, session_id, step_id, trace_id, sequence,"
                        + " occurred_at, payload_json FROM agent_run_event"
                        + " WHERE run_id = ? ORDER BY sequence, occurred_at, event_id",
                this::mapRow,
                runId);
    }

    private boolean exists(String eventId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM agent_run_event WHERE event_id = ?",
                Integer.class,
                eventId);
        return count != null && count > 0;
    }

    private boolean existsRunSequence(String runId, long sequence) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM agent_run_event WHERE run_id = ? AND sequence = ?",
                Integer.class,
                runId,
                sequence);
        return count != null && count > 0;
    }

    private RuntimeEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new RuntimeEvent(
                rs.getString("event_id"),
                RuntimeEventType.valueOf(rs.getString("event_type")),
                rs.getString("run_id"),
                rs.getString("session_id"),
                rs.getString("step_id"),
                rs.getString("trace_id"),
                rs.getLong("sequence"),
                instant(rs.getTimestamp("occurred_at")),
                map(rs.getString("payload_json")));
    }

    private String json(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Map.of() : value);
        } catch (Exception e) {
            throw new IllegalStateException("运行事件序列化失败", e);
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
