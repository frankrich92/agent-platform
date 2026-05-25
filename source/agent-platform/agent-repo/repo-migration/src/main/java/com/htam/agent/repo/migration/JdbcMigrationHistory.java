package com.htam.agent.repo.migration;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

public class JdbcMigrationHistory implements MigrationHistory {

    private final DataSource dataSource;
    private final String tableName;

    public JdbcMigrationHistory(DataSource dataSource, String tableName) {
        if (dataSource == null) {
            throw new IllegalArgumentException("dataSource 不能为空");
        }
        this.dataSource = dataSource;
        this.tableName = sanitize(tableName == null || tableName.isBlank()
                ? "agent_migration_history"
                : tableName);
        ensureTable();
    }

    @Override
    public Optional<String> currentVersion() {
        return appliedSteps().stream()
                .map(MigrationStep::version)
                .max(Comparator.naturalOrder());
    }

    @Override
    public void recordApplied(MigrationStep step) {
        execute("DELETE FROM " + tableName + " WHERE version = ?", statement -> statement.setString(1, step.version()));
        execute(
                "INSERT INTO " + tableName + " (version, description, script_resource, applied_at) VALUES (?, ?, ?, ?)",
                statement -> {
                    statement.setString(1, step.version());
                    statement.setString(2, step.description());
                    statement.setString(3, step.scriptResource());
                    statement.setTimestamp(4, Timestamp.from(Instant.now()));
                });
    }

    @Override
    public List<MigrationStep> appliedSteps() {
        List<MigrationStep> steps = new ArrayList<>();
        query("SELECT version, description, script_resource FROM " + tableName + " ORDER BY version", resultSet -> {
            while (resultSet.next()) {
                steps.add(new MigrationStep(
                        resultSet.getString("version"),
                        resultSet.getString("description"),
                        resultSet.getString("script_resource")));
            }
        });
        return steps;
    }

    private void ensureTable() {
        execute("CREATE TABLE IF NOT EXISTS " + tableName + " ("
                + "version VARCHAR(128) PRIMARY KEY,"
                + "description VARCHAR(512) NOT NULL,"
                + "script_resource VARCHAR(1024),"
                + "applied_at TIMESTAMP NOT NULL"
                + ")", statement -> {
                });
    }

    private void execute(String sql, SqlBinder binder) {
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(sql)) {
            binder.bind(statement);
            statement.executeUpdate();
        } catch (Exception e) {
            throw new IllegalStateException("migration history 写入失败", e);
        }
    }

    private void query(String sql, ResultSetConsumer consumer) {
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            consumer.accept(resultSet);
        } catch (Exception e) {
            throw new IllegalStateException("migration history 查询失败", e);
        }
    }

    private static String sanitize(String name) {
        if (!name.matches("[A-Za-z0-9_]+")) {
            throw new IllegalArgumentException("非法 migration history 表名: " + name);
        }
        return name;
    }

    @FunctionalInterface
    private interface SqlBinder {
        void bind(java.sql.PreparedStatement statement) throws Exception;
    }

    @FunctionalInterface
    private interface ResultSetConsumer {
        void accept(ResultSet resultSet) throws Exception;
    }
}
