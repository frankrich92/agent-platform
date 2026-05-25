package com.htam.agent.governance.approval;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcApprovalLedgerTest {

    private JdbcDataSource dataSource;
    private JdbcApprovalLedger ledger;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:approval_" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE governance_approval_request (
                      approval_id VARCHAR(128) PRIMARY KEY,
                      run_id VARCHAR(128),
                      step_id VARCHAR(128),
                      requester VARCHAR(128),
                      action VARCHAR(256),
                      decision VARCHAR(32),
                      created_at TIMESTAMP,
                      context_json CLOB
                    )
                    """);
        }
        ledger = new JdbcApprovalLedger(dataSource);
    }

    @Test
    void createsFindsDecidesAndListsPendingApprovals() {
        Instant createdAt = Instant.parse("2026-05-25T01:00:00Z");
        ApprovalRequest request = new ApprovalRequest(
                "approval-1",
                "run-1",
                "step-1",
                "alice",
                "workspace.write",
                null,
                createdAt,
                Map.of("risk", "high"));

        ledger.create(request);

        ApprovalRequest found = ledger.findById("approval-1").orElseThrow();
        assertEquals(ApprovalDecision.PENDING, found.decision());
        assertEquals("high", found.context().get("risk"));
        assertEquals(1, ledger.listPendingByRunId("run-1").size());

        ApprovalRequest decided = ledger.decide("approval-1", ApprovalDecision.APPROVED, "reviewer-1");

        assertEquals(ApprovalDecision.APPROVED, decided.decision());
        assertEquals("reviewer-1", decided.context().get("reviewer"));
        assertTrue(ledger.listPendingByRunId("run-1").isEmpty());
        assertEquals(ApprovalDecision.APPROVED, ledger.findById("approval-1").orElseThrow().decision());
    }

    @Test
    void rejectsUnsafeTableNames() {
        assertThrows(IllegalArgumentException.class,
                () -> new JdbcApprovalLedger(dataSource, "governance_approval_request;drop table x"));
    }
}
