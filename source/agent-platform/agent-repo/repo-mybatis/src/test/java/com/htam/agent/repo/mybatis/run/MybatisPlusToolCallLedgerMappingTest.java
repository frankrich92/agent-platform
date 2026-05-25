package com.htam.agent.repo.mybatis.run;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.htam.agent.common.entity.AgentToolCall;
import com.htam.agent.runtime.ToolCall;
import com.htam.agent.runtime.ToolCallPolicy;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MybatisPlusToolCallLedgerMappingTest {

    private final MybatisPlusToolCallLedger ledger = new MybatisPlusToolCallLedger(null);

    @Test
    void mapsToolCallToEntity() {
        ToolCall toolCall = new ToolCall(
                "tool-call-1",
                "run-1",
                "get_time",
                ToolCallPolicy.ALLOW,
                true,
                Duration.ofMillis(25),
                120L,
                "params",
                "result",
                null,
                null,
                Map.of("risk", "low"));

        AgentToolCall entity = ledger.toEntity(toolCall);

        assertEquals("tool-call-1", entity.getToolCallId());
        assertEquals("run-1", entity.getRunId());
        assertEquals("get_time", entity.getToolName());
        assertEquals("ALLOW", entity.getPolicy());
        assertEquals(25L, entity.getDurationMillis());
        assertEquals(120L, entity.getCostMicros());
        assertTrue(entity.getAuditTagsJson().contains("risk"));
    }

    @Test
    void mapsEntityToToolCallWithPolicyFallbacks() {
        AgentToolCall entity = new AgentToolCall();
        entity.setToolCallId("tool-call-2");
        entity.setRunId("run-2");
        entity.setToolName("search");
        entity.setPolicy("UNKNOWN");
        entity.setReadOnly(null);
        entity.setDurationMillis(null);
        entity.setCostMicros(null);
        entity.setAuditTagsJson("{\"policy\":\"fallback\"}");

        ToolCall toolCall = ledger.toRecord(entity);

        assertEquals(ToolCallPolicy.ASK, toolCall.policy());
        assertEquals(Duration.ZERO, toolCall.duration());
        assertEquals(0L, toolCall.costMicros());
        assertEquals("fallback", toolCall.auditTags().get("policy"));
    }
}
