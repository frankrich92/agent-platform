package com.htam.agent.runtime.agentscope;

import io.agentscope.core.session.Session;
import io.agentscope.core.state.SessionKey;
import io.agentscope.core.state.SimpleSessionKey;
import io.agentscope.core.state.State;
import io.agentscope.core.util.JsonUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * PostgreSQL-backed AgentScope session store.
 */
public class PostgresSession implements Session {

    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    private final DataSource dataSource;
    private final String tableName;

    public PostgresSession(DataSource dataSource, String tableName) {
        if (dataSource == null) {
            throw new IllegalArgumentException("DataSource cannot be null");
        }
        validateIdentifier(tableName);
        this.dataSource = dataSource;
        this.tableName = tableName;
        createTableIfNotExists();
    }

    @Override
    public void save(SessionKey sessionKey, String key, State value) {
        String sessionId = sessionId(sessionKey);
        validateStateKey(key);
        String sql = """
                INSERT INTO %s (session_id, state_key, item_index, state_data)
                VALUES (?, ?, 0, ?)
                ON CONFLICT (session_id, state_key, item_index)
                DO UPDATE SET state_data = EXCLUDED.state_data, updated_at = CURRENT_TIMESTAMP
                """.formatted(tableName);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, sessionId);
            statement.setString(2, key);
            statement.setString(3, JsonUtils.getJsonCodec().toJson(value));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save state: " + key, e);
        }
    }

    @Override
    public void save(SessionKey sessionKey, String key, List<? extends State> values) {
        String sessionId = sessionId(sessionKey);
        validateStateKey(key);
        String deleteSql = "DELETE FROM %s WHERE session_id = ? AND state_key = ?".formatted(tableName);
        String insertSql = """
                INSERT INTO %s (session_id, state_key, item_index, state_data)
                VALUES (?, ?, ?, ?)
                """.formatted(tableName);
        try (Connection connection = dataSource.getConnection()) {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSql)) {
                    deleteStatement.setString(1, sessionId);
                    deleteStatement.setString(2, key);
                    deleteStatement.executeUpdate();
                }
                try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
                    for (int i = 0; i < values.size(); i++) {
                        insertStatement.setString(1, sessionId);
                        insertStatement.setString(2, key);
                        insertStatement.setInt(3, i);
                        insertStatement.setString(4, JsonUtils.getJsonCodec().toJson(values.get(i)));
                        insertStatement.addBatch();
                    }
                    insertStatement.executeBatch();
                }
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save state list: " + key, e);
        }
    }

    @Override
    public <T extends State> Optional<T> get(SessionKey sessionKey, String key, Class<T> type) {
        String sessionId = sessionId(sessionKey);
        validateStateKey(key);
        String sql = """
                SELECT state_data FROM %s
                WHERE session_id = ? AND state_key = ? AND item_index = 0
                """.formatted(tableName);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, sessionId);
            statement.setString(2, key);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(JsonUtils.getJsonCodec().fromJson(resultSet.getString("state_data"), type));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get state: " + key, e);
        }
    }

    @Override
    public <T extends State> List<T> getList(SessionKey sessionKey, String key, Class<T> type) {
        String sessionId = sessionId(sessionKey);
        validateStateKey(key);
        String sql = """
                SELECT state_data FROM %s
                WHERE session_id = ? AND state_key = ?
                ORDER BY item_index
                """.formatted(tableName);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, sessionId);
            statement.setString(2, key);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<T> values = new ArrayList<>();
                while (resultSet.next()) {
                    values.add(JsonUtils.getJsonCodec().fromJson(resultSet.getString("state_data"), type));
                }
                return values;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get state list: " + key, e);
        }
    }

    @Override
    public boolean exists(SessionKey sessionKey) {
        String sql = "SELECT 1 FROM %s WHERE session_id = ? LIMIT 1".formatted(tableName);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, sessionId(sessionKey));
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check session existence", e);
        }
    }

    @Override
    public void delete(SessionKey sessionKey) {
        String sql = "DELETE FROM %s WHERE session_id = ?".formatted(tableName);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, sessionId(sessionKey));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete session", e);
        }
    }

    @Override
    public void delete(SessionKey sessionKey, String key) {
        validateStateKey(key);
        String sql = "DELETE FROM %s WHERE session_id = ? AND state_key = ?".formatted(tableName);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, sessionId(sessionKey));
            statement.setString(2, key);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete state: " + key, e);
        }
    }

    @Override
    public Set<SessionKey> listSessionKeys() {
        String sql = "SELECT DISTINCT session_id FROM %s ORDER BY session_id".formatted(tableName);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            Set<SessionKey> keys = new LinkedHashSet<>();
            while (resultSet.next()) {
                keys.add(SimpleSessionKey.of(resultSet.getString("session_id")));
            }
            return keys;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list sessions", e);
        }
    }

    private void createTableIfNotExists() {
        String sql = """
                CREATE TABLE IF NOT EXISTS %s (
                    session_id varchar(255) NOT NULL,
                    state_key varchar(255) NOT NULL,
                    item_index integer NOT NULL DEFAULT 0,
                    state_data text NOT NULL,
                    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (session_id, state_key, item_index)
                )
                """.formatted(tableName);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create session table: " + tableName, e);
        }
    }

    private String sessionId(SessionKey sessionKey) {
        if (sessionKey == null) {
            throw new IllegalArgumentException("Session key cannot be null");
        }
        String sessionId = sessionKey.toIdentifier();
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }
        if (sessionId.contains("/") || sessionId.contains("\\")) {
            throw new IllegalArgumentException("Session ID cannot contain path separators");
        }
        if (sessionId.length() > 255) {
            throw new IllegalArgumentException("Session ID cannot exceed 255 characters");
        }
        return sessionId;
    }

    private void validateStateKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("State key cannot be null or empty");
        }
        if (key.length() > 255) {
            throw new IllegalArgumentException("State key cannot exceed 255 characters");
        }
    }

    private void validateIdentifier(String identifier) {
        if (identifier == null || !IDENTIFIER_PATTERN.matcher(identifier).matches()) {
            throw new IllegalArgumentException("Invalid database identifier: " + identifier);
        }
    }
}
