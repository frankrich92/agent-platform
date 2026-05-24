package com.htam.agent.runtime.planning;

import java.util.List;

public record DoomLoopDecision(
        boolean detected,
        boolean humanTakeoverRequired,
        List<String> reasons) {

    public DoomLoopDecision {
        reasons = reasons == null ? List.of() : List.copyOf(reasons);
    }
}
