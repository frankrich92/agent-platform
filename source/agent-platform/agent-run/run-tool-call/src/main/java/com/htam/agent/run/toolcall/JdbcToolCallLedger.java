package com.htam.agent.run.toolcall;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htam.agent.runtime.ToolCall;
import com.htam.agent.runtime.ToolCallPolicy;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;

public class JdbcToolCallLedger implements ToolCallLedger {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcToolCallLedger(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Override
    public void append(ToolCall toolCall) {
        if (toolCall == null) {
            return;
        }
        if (exists(toolCall.toolCallId())) {
            update(toolCall);
        } else {
            insert(toolCall);
        }
    }

    @Override
    public List<ToolCall> listByRunId(String runId) {
        return jdbcTemplate.query(
                "SELECT tool_call_id, run_id, tool_name, policy, read_only, duration_millis,"
                        + " cost_micros, parameter_summary, result_summary, error_code,"
                        + " error_message, audit_tags_json FROM agent_tool_call"
                        + " WHERE run_id = ? ORDER BY created_at, tool_call_id",
                this::mapRow,
                runId);
    }

    private boolean exists(String toolCallId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM agent_tool_call WHERE tool_call_id = ?",
                Integer.class,
                toolCallId);
        return count != null && count > 0;
    }

    private void insert(ToolCall toolCall) {
        jdbcTemplate.update(
                "INSERT INTO agent_tool_call"
                        + " (tool_call_id, run_id, tool_name, policy, read_only, duration_millis,"
                        + " cost_micros, parameter_summary, result_summary, error_code, error_message,"
                        + " audit_tags_json)"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                toolCall.toolCallId(),
                toolCall.runId(),
                toolCall.toolName(),
                toolCall.policy().name(),
                toolCall.readOnly(),
                toolCall.duration().toMillis(),
                toolCall.costMicros(),
                toolCall.parameterSummary(),
                toolCall.resultSummary(),
                toolCall.errorCode(),
                toolCall.errorMessage(),
                json(toolCall.auditTags()));
    }

    private void update(ToolCall toolCall) {
        jdbcTemplate.update(
                "UPDATE agent_tool_call SET run_id = ?, tool_name = ?, policy = ?, read_only = ?,"
                        + " duration_millis = ?, cost_micros = ?, parameter_summary = ?,"
                        + " result_summary = ?, error_code = ?, error_message = ?, audit_tags_json = ?"
                        + " WHERE tool_call_id = ?",
                toolCall.runId(),
                toolCall.toolName(),
                toolCall.policy().name(),
                toolCall.readOnly(),
                toolCall.duration().toMillis(),
                toolCall.costMicros(),
                toolCall.parameterSummary(),
                toolCall.resultSummary(),
                toolCall.errorCode(),
                toolCall.errorMessage(),
                json(toolCall.auditTags()),
                toolCall.toolCallId());
    }

    private ToolCall mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new ToolCall(
                rs.getString("tool_call_id"),
                rs.getString("run_id"),
                rs.getString("tool_name"),
                ToolCallPolicy.valueOf(rs.getString("policy")),
                rs.getBoolean("read_only"),
                Duration.ofMillis(rs.getLong("duration_millis")),
                rs.getLong("cost_micros"),
                rs.getString("parameter_summary"),
                rs.getString("result_summary"),
                rs.getString("error_code"),
                rs.getString("error_message"),
                map(rs.getString("audit_tags_json")));
    }

    private String json(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Map.of() : value);
        } catch (Exception e) {
            throw new IllegalStateException("工具调用审计标签序列化失败", e);
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
}
