package com.htam.agent.repo.mybatis.run;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.htam.agent.common.entity.AgentRun;
import com.htam.agent.run.AgentRunRecord;
import com.htam.agent.runtime.AgentRunStatus;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MybatisPlusAgentRunLedgerMappingTest {

    private final MybatisPlusAgentRunLedger ledger = new MybatisPlusAgentRunLedger(null);

    @Test
    void mapsRunRecordToEntity() {
        Instant startedAt = Instant.parse("2026-05-25T10:15:30Z");
        AgentRunRecord record = new AgentRunRecord(
                "run-1",
                100L,
                "session-1",
                AgentRunStatus.RUNNING,
                "input",
                "output",
                "trace-1",
                startedAt,
                null,
                Map.of("title", "run title", "inputPreview", "input"));

        AgentRun entity = ledger.toEntity(record);

        assertEquals("run-1", entity.getRunId());
        assertEquals(100L, entity.getAgentId());
        assertEquals("session-1", entity.getSessionId());
        assertEquals("RUNNING", entity.getStatus());
        assertEquals("trace-1", entity.getTraceId());
        assertEquals(LocalDateTime.ofInstant(startedAt, java.time.ZoneId.systemDefault()), entity.getStartedAt());
        assertTrue(entity.getMetadataJson().contains("title"));
    }

    @Test
    void mapsEntityToRecordWithFallbackStatusAndMetadata() {
        AgentRun entity = new AgentRun();
        entity.setRunId("run-2");
        entity.setAgentId(200L);
        entity.setSessionId("session-2");
        entity.setStatus("UNKNOWN");
        entity.setStartedAt(LocalDateTime.parse("2026-05-25T10:15:30"));
        entity.setMetadataJson("{\"title\":\"fallback\"}");

        AgentRunRecord record = ledger.toRecord(entity);

        assertEquals("run-2", record.runId());
        assertEquals(AgentRunStatus.ACCEPTED, record.status());
        assertEquals("fallback", record.metadata().get("title"));
    }
}
