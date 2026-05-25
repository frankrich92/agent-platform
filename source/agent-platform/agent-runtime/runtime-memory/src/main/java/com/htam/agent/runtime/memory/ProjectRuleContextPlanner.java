package com.htam.agent.runtime.memory;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProjectRuleContextPlanner {

    public List<RuntimeContextSegment> planSegments(List<ProjectRuleContextSource> sources) {
        List<ProjectRuleContextSource> sourceList = sources == null ? List.of() : List.copyOf(sources);
        return sourceList.stream()
                .filter(source -> !source.content().isBlank())
                .sorted(Comparator.comparing(ProjectRuleContextSource::precedence).reversed()
                        .thenComparing(ProjectRuleContextSource::sourceRef))
                .filter(new java.util.function.Predicate<>() {
                    private final Map<String, String> selectedConflicts = new LinkedHashMap<>();

                    @Override
                    public boolean test(ProjectRuleContextSource source) {
                        return selectedConflicts.putIfAbsent(source.conflictKey(), source.sourceRef()) == null;
                    }
                })
                .map(this::toSegment)
                .toList();
    }

    private RuntimeContextSegment toSegment(ProjectRuleContextSource source) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("sourceRef", source.sourceRef());
        attributes.put("scope", source.scope().name());
        attributes.put("precedence", source.precedence());
        attributes.put("conflictKey", source.conflictKey());
        attributes.put("permissionTags", source.permissionTags());
        attributes.put("sourceType", "project-rule");
        return new RuntimeContextSegment(
                "project-rule:" + source.sourceRef(),
                ContextSegmentKind.RUNTIME_HINT,
                source.sourceRef(),
                estimateTokens(source.content()),
                false,
                attributes);
    }

    private static int estimateTokens(String content) {
        return Math.max(1, (content == null ? 0 : content.length()) / 4);
    }
}
