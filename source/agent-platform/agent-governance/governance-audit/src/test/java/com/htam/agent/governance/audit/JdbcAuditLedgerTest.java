package com.htam.agent.governance.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcAuditLedgerTest {

    private JdbcDataSource dataSource;
    private JdbcAuditLedger ledger;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:audit_" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE governance_audit_event (
                      audit_id VARCHAR(128) PRIMARY KEY,
                      trace_id VARCHAR(128),
                      run_id VARCHAR(128),
                      actor VARCHAR(128),
                      action VARCHAR(256),
                      severity VARCHAR(32),
                      occurred_at TIMESTAMP,
                      attributes_json CLOB
                    )
                    """);
        }
        ledger = new JdbcAuditLedger(dataSource);
    }

    @Test
    void appendsAndListsAuditEventsByRunAndTrace() {
        Instant base = Instant.parse("2026-05-25T01:00:00Z");
        ledger.append(new AuditEvent(
                "audit-2",
                "trace-1",
                "run-1",
                "alice",
                "tool.execute",
                AuditSeverity.WARN,
                base.plusSeconds(10),
                Map.of("tool", "shell")));
        ledger.append(new AuditEvent(
                "audit-1",
                "trace-1",
                "run-1",
                "alice",
                "run.start",
                AuditSeverity.INFO,
                base,
                Map.of("origin", "rest")));
        ledger.append(new AuditEvent(
                "audit-3",
                "trace-2",
                "run-2",
                "bob",
                "run.start",
                AuditSeverity.INFO,
                base.plusSeconds(20),
                Map.of()));

        List<AuditEvent> runEvents = ledger.listByRunId("run-1");
        assertEquals(List.of("audit-1", "audit-2"), runEvents.stream().map(AuditEvent::auditId).toList());
        assertEquals("shell", runEvents.get(1).attributes().get("tool"));

        List<AuditEvent> traceEvents = ledger.listByTraceId("trace-1");
        assertEquals(List.of("audit-1", "audit-2"), traceEvents.stream().map(AuditEvent::auditId).toList());
    }

    @Test
    void rejectsUnsafeTableNames() {
        assertThrows(IllegalArgumentException.class,
                () -> new JdbcAuditLedger(dataSource, "governance_audit_event;drop table x"));
    }
}
