package com.htam.agent.run;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htam.agent.runtime.AgentRunStatus;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;

public class JdbcAgentRunLedger implements AgentRunLedger {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcAgentRunLedger(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Override
    public AgentRunRecord save(AgentRunRecord record) {
        if (exists(record.runId())) {
            update(record);
        } else {
            insert(record);
        }
        return record;
    }

    @Override
    public Optional<AgentRunRecord> findById(String runId) {
        if (runId == null || runId.isBlank()) {
            return Optional.empty();
        }
        List<AgentRunRecord> records = jdbcTemplate.query(
                "SELECT run_id, agent_id, session_id, status, input, output, trace_id,"
                        + " started_at, ended_at, metadata_json FROM agent_run WHERE run_id = ?",
                this::mapRow,
                runId);
        return records.stream().findFirst();
    }

    @Override
    public List<AgentRunRecord> listBySessionId(String sessionId) {
        return search(AgentRunSearchQuery.bySessionId(sessionId));
    }

    @Override
    public List<AgentRunRecord> search(AgentRunSearchQuery query) {
        if (query == null || query.empty()) {
            return List.of();
        }
        StringBuilder sql = new StringBuilder(
                "SELECT run_id, agent_id, session_id, status, input, output, trace_id,"
                        + " started_at, ended_at, metadata_json FROM agent_run WHERE 1 = 1");
        List<Object> args = new java.util.ArrayList<>();
        if (query.sessionId() != null) {
            sql.append(" AND session_id = ?");
            args.add(query.sessionId());
        }
        if (query.status() != null) {
            sql.append(" AND status = ?");
            args.add(query.status().name());
        }
        if (query.keyword() != null) {
            sql.append(" AND (LOWER(run_id) LIKE ?"
                    + " OR LOWER(COALESCE(input, '')) LIKE ?"
                    + " OR LOWER(COALESCE(output, '')) LIKE ?"
                    + " OR LOWER(COALESCE(trace_id, '')) LIKE ?"
                    + " OR LOWER(COALESCE(metadata_json, '')) LIKE ?)");
            String keyword = "%" + query.keyword().toLowerCase() + "%";
            args.add(keyword);
            args.add(keyword);
            args.add(keyword);
            args.add(keyword);
            args.add(keyword);
        }
        sql.append(" ORDER BY started_at DESC LIMIT ?");
        args.add(query.limit());
        return jdbcTemplate.query(sql.toString(), this::mapRow, args.toArray());
    }

    private boolean exists(String runId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM agent_run WHERE run_id = ?",
                Integer.class,
                runId);
        return count != null && count > 0;
    }

    private void insert(AgentRunRecord record) {
        jdbcTemplate.update(
                "INSERT INTO agent_run"
                        + " (run_id, agent_id, session_id, status, input, output, trace_id,"
                        + " started_at, ended_at, metadata_json, created_at, updated_at)"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                record.runId(),
                record.agentId(),
                record.sessionId(),
                record.status().name(),
                record.input(),
                record.output(),
                record.traceId(),
                timestamp(record.startedAt()),
                timestamp(record.endedAt()),
                json(record.metadata()),
                timestamp(Instant.now()),
                timestamp(Instant.now()));
    }

    private void update(AgentRunRecord record) {
        jdbcTemplate.update(
                "UPDATE agent_run SET agent_id = ?, session_id = ?, status = ?, input = ?, output = ?,"
                        + " trace_id = ?, started_at = ?, ended_at = ?, metadata_json = ?, updated_at = ?"
                        + " WHERE run_id = ?",
                record.agentId(),
                record.sessionId(),
                record.status().name(),
                record.input(),
                record.output(),
                record.traceId(),
                timestamp(record.startedAt()),
                timestamp(record.endedAt()),
                json(record.metadata()),
                timestamp(Instant.now()),
                record.runId());
    }

    private AgentRunRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new AgentRunRecord(
                rs.getString("run_id"),
                nullableLong(rs, "agent_id"),
                rs.getString("session_id"),
                AgentRunStatus.valueOf(rs.getString("status")),
                rs.getString("input"),
                rs.getString("output"),
                rs.getString("trace_id"),
                instant(rs.getTimestamp("started_at")),
                instant(rs.getTimestamp("ended_at")),
                map(rs.getString("metadata_json")));
    }

    private String json(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Map.of() : value);
        } catch (Exception e) {
            throw new IllegalStateException("运行元数据序列化失败", e);
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

    private static Long nullableLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }
}
