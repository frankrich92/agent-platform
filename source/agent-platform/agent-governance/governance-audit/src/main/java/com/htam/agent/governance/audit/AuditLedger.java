package com.htam.agent.governance.audit;

import java.util.List;

public interface AuditLedger {

    AuditEvent append(AuditEvent event);

    List<AuditEvent> listByRunId(String runId);

    List<AuditEvent> listByTraceId(String traceId);
}
