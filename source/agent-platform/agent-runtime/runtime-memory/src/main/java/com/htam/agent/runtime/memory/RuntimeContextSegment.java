package com.htam.agent.runtime.memory;

import java.util.Map;

public record RuntimeContextSegment(
        String segmentId,
        ContextSegmentKind kind,
        String contentRef,
        int tokenEstimate,
        boolean compressible,
        Map<String, Object> attributes) {

    public RuntimeContextSegment {
        kind = kind == null ? ContextSegmentKind.RUNTIME_HINT : kind;
        contentRef = contentRef == null || contentRef.isBlank()
                ? kind.name().toLowerCase()
                : contentRef;
        tokenEstimate = Math.max(0, tokenEstimate);
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
