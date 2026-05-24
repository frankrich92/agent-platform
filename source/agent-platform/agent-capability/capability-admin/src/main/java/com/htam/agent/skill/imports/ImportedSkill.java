package com.htam.agent.skill.imports;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Platform-neutral skill package metadata parsed from an import source.
 */
public record ImportedSkill(
        String name,
        String description,
        String skillContent,
        Map<String, String> resources) {

    public ImportedSkill {
        resources = resources == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(resources));
    }
}
