package com.htam.agent.repo.mybatis.run;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.htam.agent.common.entity.AgentRunStep;
import com.htam.agent.runtime.AgentRunStatus;
import com.htam.agent.runtime.RunStep;
import com.htam.agent.runtime.RunStepType;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MybatisPlusRunStepLedgerMappingTest {

    private final MybatisPlusRunStepLedger ledger = new MybatisPlusRunStepLedger(null);

    @Test
    void mapsRunStepToEntity() {
        Instant startedAt = Instant.parse("2026-05-25T10:15:30Z");
        RunStep step = new RunStep(
                "step-1",
                "run-1",
                RunStepType.TOOL_CALL,
                AgentRunStatus.SUCCEEDED,
                startedAt,
                null,
                Map.of("tool", "time"));

        AgentRunStep entity = ledger.toEntity(step);

        assertEquals("step-1", entity.getStepId());
        assertEquals("run-1", entity.getRunId());
        assertEquals("TOOL_CALL", entity.getStepType());
        assertEquals("SUCCEEDED", entity.getStatus());
        assertEquals(LocalDateTime.ofInstant(startedAt, java.time.ZoneId.systemDefault()), entity.getStartedAt());
        assertTrue(entity.getSummaryJson().contains("tool"));
    }

    @Test
    void mapsEntityToRunStepWithEnumFallbacks() {
        AgentRunStep entity = new AgentRunStep();
        entity.setStepId("step-2");
        entity.setRunId("run-2");
        entity.setStepType("UNKNOWN");
        entity.setStatus("UNKNOWN");
        entity.setSummaryJson("{\"phase\":\"fallback\"}");

        RunStep step = ledger.toRecord(entity);

        assertEquals(RunStepType.OTHER, step.stepType());
        assertEquals(AgentRunStatus.ACCEPTED, step.status());
        assertEquals("fallback", step.summary().get("phase"));
    }
}
