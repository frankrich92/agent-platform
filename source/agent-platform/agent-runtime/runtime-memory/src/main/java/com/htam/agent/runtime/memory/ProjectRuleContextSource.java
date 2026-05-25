package com.htam.agent.runtime.memory;

import java.util.List;

public record ProjectRuleContextSource(
        String sourceRef,
        MemoryScope scope,
        String content,
        int precedence,
        String conflictKey,
        List<String> permissionTags) {

    public ProjectRuleContextSource {
        if (sourceRef == null || sourceRef.isBlank()) {
            throw new IllegalArgumentException("sourceRef 不能为空");
        }
        scope = scope == null ? MemoryScope.PROFILE : scope;
        content = content == null ? "" : content.strip();
        precedence = Math.max(0, precedence);
        conflictKey = conflictKey == null || conflictKey.isBlank() ? sourceRef : conflictKey;
        permissionTags = permissionTags == null ? List.of() : List.copyOf(permissionTags);
    }
}
