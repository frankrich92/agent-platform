package com.htam.agent.capability.skill;

public record SkillDependency(
        String skillId,
        String versionRange,
        boolean optional) {

    public SkillDependency {
        if (skillId == null || skillId.isBlank()) {
            throw new IllegalArgumentException("skillId 不能为空");
        }
        versionRange = versionRange == null || versionRange.isBlank() ? "*" : versionRange;
    }
}
