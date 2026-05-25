package com.htam.agent.repo.migration;

import java.nio.charset.StandardCharsets;
import javax.sql.DataSource;

public class SqlResourceMigrationStepHandler implements MigrationStepHandler {

    private final DataSource dataSource;
    private final ClassLoader classLoader;

    public SqlResourceMigrationStepHandler(DataSource dataSource, ClassLoader classLoader) {
        if (dataSource == null) {
            throw new IllegalArgumentException("dataSource 不能为空");
        }
        this.dataSource = dataSource;
        this.classLoader = classLoader == null ? Thread.currentThread().getContextClassLoader() : classLoader;
    }

    @Override
    public void apply(MigrationStep step) {
        if (step.scriptResource() == null || step.scriptResource().isBlank()) {
            return;
        }
        try (var input = classLoader.getResourceAsStream(step.scriptResource())) {
            if (input == null) {
                throw new IllegalArgumentException("migration resource 不存在: " + step.scriptResource());
            }
            String sql = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            try (var connection = dataSource.getConnection();
                 var statement = connection.createStatement()) {
                statement.execute(sql);
            }
        } catch (Exception e) {
            throw new IllegalStateException("migration step 执行失败: " + step.version(), e);
        }
    }
}
