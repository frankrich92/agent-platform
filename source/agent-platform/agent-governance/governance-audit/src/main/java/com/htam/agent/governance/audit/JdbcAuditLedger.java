package com.htam.agent.governance.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

public class JdbcAuditLedger implements AuditLedger {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final DataSource dataSource;
    private final ObjectMapper objectMapper;
    private final String tableName;

    public JdbcAuditLedger(DataSource dataSource) {
        this(dataSource, "governance_audit_event");
    }

    public JdbcAuditLedger(DataSource dataSource, String tableName) {
        this(dataSource, tableName, new ObjectMapper());
    }

    public JdbcAuditLedger(DataSource dataSource, String tableName, ObjectMapper objectMapper) {
        if (dataSource == null) {
            throw new IllegalArgumentException("dataSource 不能为空");
        }
        this.dataSource = dataSource;
        this.tableName = validateTableName(tableName);
        this.objectMapper = objectMapper == null ? new ObjectMapper() : objectMapper;
    }

    @Override
    public AuditEvent append(AuditEvent event) {
        String sql = "INSERT INTO " + tableName
                + " (audit_id, trace_id, run_id, actor, action, severity, occurred_at, attributes_json)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        executeUpdate(sql, statement -> {
            statement.setString(1, event.auditId());
            statement.setString(2, event.traceId());
            statement.setString(3, event.runId());
            statement.setString(4, event.actor());
            statement.setString(5, event.action());
            statement.setString(6, event.severity().name());
            statement.setTimestamp(7, Timestamp.from(event.occurredAt()));
            statement.setString(8, writeJson(event.attributes()));
        });
        return event;
    }

    @Override
    public List<AuditEvent> listByRunId(String runId) {
        String sql = "SELECT audit_id, trace_id, run_id, actor, action, severity, occurred_at, attributes_json"
                + " FROM " + tableName + " WHERE run_id = ? ORDER BY occurred_at";
        return executeQuery(sql, statement -> statement.setString(1, runId));
    }

    @Override
    public List<AuditEvent> listByTraceId(String traceId) {
        String sql = "SELECT audit_id, trace_id, run_id, actor, action, severity, occurred_at, attributes_json"
                + " FROM " + tableName + " WHERE trace_id = ? ORDER BY occurred_at";
        return executeQuery(sql, statement -> statement.setString(1, traceId));
    }

    private List<AuditEvent> executeQuery(String sql, SqlBinder binder) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            binder.bind(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<AuditEvent> result = new java.util.ArrayList<>();
                while (resultSet.next()) {
                    result.add(readAudit(resultSet));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("审计记录查询失败", e);
        }
    }

    private void executeUpdate(String sql, SqlBinder binder) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            binder.bind(statement);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("审计记录写入失败", e);
        }
    }

    private AuditEvent readAudit(ResultSet resultSet) throws SQLException {
        Timestamp occurredAt = resultSet.getTimestamp("occurred_at");
        return new AuditEvent(
                resultSet.getString("audit_id"),
                resultSet.getString("trace_id"),
                resultSet.getString("run_id"),
                resultSet.getString("actor"),
                resultSet.getString("action"),
                AuditSeverity.valueOf(resultSet.getString("severity")),
                occurredAt == null ? null : occurredAt.toInstant(),
                readMap(resultSet.getString("attributes_json")));
    }

    private String writeJson(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Map.of() : value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("审计属性序列化失败", e);
        }
    }

    private Map<String, Object> readMap(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (IOException e) {
            return Map.of("raw", json);
        }
    }

    private static String validateTableName(String tableName) {
        String value = tableName == null || tableName.isBlank() ? "governance_audit_event" : tableName;
        if (!value.matches("[A-Za-z0-9_.]+")) {
            throw new IllegalArgumentException("tableName 只能包含字母、数字、下划线和点号");
        }
        return value;
    }

    @FunctionalInterface
    private interface SqlBinder {
        void bind(PreparedStatement statement) throws SQLException;
    }
}
