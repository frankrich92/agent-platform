package com.htam.agent.governance.approval;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;

public class JdbcApprovalLedger implements ApprovalLedger {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final DataSource dataSource;
    private final ObjectMapper objectMapper;
    private final String tableName;

    public JdbcApprovalLedger(DataSource dataSource) {
        this(dataSource, "governance_approval_request");
    }

    public JdbcApprovalLedger(DataSource dataSource, String tableName) {
        this(dataSource, tableName, new ObjectMapper());
    }

    public JdbcApprovalLedger(DataSource dataSource, String tableName, ObjectMapper objectMapper) {
        if (dataSource == null) {
            throw new IllegalArgumentException("dataSource 不能为空");
        }
        this.dataSource = dataSource;
        this.tableName = validateTableName(tableName);
        this.objectMapper = objectMapper == null ? new ObjectMapper() : objectMapper;
    }

    @Override
    public ApprovalRequest create(ApprovalRequest request) {
        String sql = "INSERT INTO " + tableName
                + " (approval_id, run_id, step_id, requester, action, decision, created_at, context_json)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        executeUpdate(sql, statement -> {
            statement.setString(1, request.approvalId());
            statement.setString(2, request.runId());
            statement.setString(3, request.stepId());
            statement.setString(4, request.requester());
            statement.setString(5, request.action());
            statement.setString(6, request.decision().name());
            statement.setTimestamp(7, Timestamp.from(request.createdAt()));
            statement.setString(8, writeJson(request.context()));
        });
        return request;
    }

    @Override
    public ApprovalRequest decide(String approvalId, ApprovalDecision decision, String reviewer) {
        ApprovalRequest current = findById(approvalId)
                .orElseThrow(() -> new IllegalArgumentException("ApprovalRequest 不存在: " + approvalId));
        Map<String, Object> context = new LinkedHashMap<>(current.context());
        context.put("reviewer", reviewer);
        context.put("decidedAt", Instant.now().toString());
        ApprovalRequest updated = new ApprovalRequest(
                current.approvalId(),
                current.runId(),
                current.stepId(),
                current.requester(),
                current.action(),
                decision,
                current.createdAt(),
                context);
        String sql = "UPDATE " + tableName + " SET decision = ?, context_json = ? WHERE approval_id = ?";
        executeUpdate(sql, statement -> {
            statement.setString(1, updated.decision().name());
            statement.setString(2, writeJson(updated.context()));
            statement.setString(3, updated.approvalId());
        });
        return updated;
    }

    @Override
    public Optional<ApprovalRequest> findById(String approvalId) {
        String sql = "SELECT approval_id, run_id, step_id, requester, action, decision, created_at, context_json"
                + " FROM " + tableName + " WHERE approval_id = ?";
        return executeQuery(sql, statement -> statement.setString(1, approvalId)).stream().findFirst();
    }

    @Override
    public List<ApprovalRequest> listPendingByRunId(String runId) {
        if (runId == null || runId.isBlank()) {
            String sql = "SELECT approval_id, run_id, step_id, requester, action, decision, created_at, context_json"
                    + " FROM " + tableName + " WHERE decision = ? ORDER BY created_at";
            return executeQuery(sql, statement -> statement.setString(1, ApprovalDecision.PENDING.name()));
        }
        String sql = "SELECT approval_id, run_id, step_id, requester, action, decision, created_at, context_json"
                + " FROM " + tableName + " WHERE decision = ? AND run_id = ? ORDER BY created_at";
        return executeQuery(sql, statement -> {
            statement.setString(1, ApprovalDecision.PENDING.name());
            statement.setString(2, runId);
        });
    }

    private List<ApprovalRequest> executeQuery(String sql, SqlBinder binder) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            binder.bind(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<ApprovalRequest> result = new java.util.ArrayList<>();
                while (resultSet.next()) {
                    result.add(readApproval(resultSet));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("审批记录查询失败", e);
        }
    }

    private void executeUpdate(String sql, SqlBinder binder) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            binder.bind(statement);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("审批记录写入失败", e);
        }
    }

    private ApprovalRequest readApproval(ResultSet resultSet) throws SQLException {
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        return new ApprovalRequest(
                resultSet.getString("approval_id"),
                resultSet.getString("run_id"),
                resultSet.getString("step_id"),
                resultSet.getString("requester"),
                resultSet.getString("action"),
                ApprovalDecision.valueOf(resultSet.getString("decision")),
                createdAt == null ? null : createdAt.toInstant(),
                readMap(resultSet.getString("context_json")));
    }

    private String writeJson(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Map.of() : value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("审批上下文序列化失败", e);
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
        String value = tableName == null || tableName.isBlank() ? "governance_approval_request" : tableName;
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
