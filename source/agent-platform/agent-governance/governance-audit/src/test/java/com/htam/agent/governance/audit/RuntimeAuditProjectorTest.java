package com.htam.agent.governance.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.htam.agent.runtime.RuntimeEvent;
import com.htam.agent.runtime.RuntimeEventType;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RuntimeAuditProjectorTest {

    @Test
    void projectsRuntimeEventIntoAuditLedger() {
        InMemoryAuditLedger ledger = new InMemoryAuditLedger();
        RuntimeAuditProjector projector = new RuntimeAuditProjector();
        RuntimeEvent event = new RuntimeEvent("event-1", RuntimeEventType.RUN_FAILED,
                "run-1", "session-1", null, "trace-1", 1, Instant.now(), Map.of("error", "boom"));

        ledger.append(projector.fromRuntimeEvent(event));

        assertEquals(1, ledger.listByRunId("run-1").size());
        assertEquals(AuditSeverity.ERROR, ledger.listByTraceId("trace-1").getFirst().severity());
    }
}
