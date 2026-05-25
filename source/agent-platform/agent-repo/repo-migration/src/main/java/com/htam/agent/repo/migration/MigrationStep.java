package com.htam.agent.repo.migration;

public record MigrationStep(String version, String description, String scriptResource) {

    public MigrationStep {
        if (version == null || version.isBlank()) {
            throw new IllegalArgumentException("version 不能为空");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("description 不能为空");
        }
    }
}
