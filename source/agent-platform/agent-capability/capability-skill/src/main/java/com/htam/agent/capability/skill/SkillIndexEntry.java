package com.htam.agent.capability.skill;

import java.util.List;

public record SkillIndexEntry(
        String skillId,
        String name,
        String version,
        String description,
        List<String> tags,
        SkillReleaseStatus releaseStatus,
        String contentRef) {

    public SkillIndexEntry {
        if (skillId == null || skillId.isBlank()) {
            throw new IllegalArgumentException("skillId 不能为空");
        }
        tags = tags == null ? List.of() : List.copyOf(tags);
        releaseStatus = releaseStatus == null ? SkillReleaseStatus.DRAFT : releaseStatus;
    }
}
