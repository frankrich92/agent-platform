package com.htam.agent.run;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.htam.agent.runtime.AgentRunStatus;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class JdbcAgentRunLedgerTest {

    @Test
    void searchFiltersBySessionStatusAndKeyword() {
        JdbcDataSource dataSource = dataSource();
        createSchema(dataSource);
        JdbcAgentRunLedger ledger = new JdbcAgentRunLedger(dataSource);
        ledger.save(new AgentRunRecord(
                "run-search-1",
                1L,
                "session-1",
                AgentRunStatus.SUCCEEDED,
                "Generate weekly sales summary",
                "done",
                "trace-1",
                Instant.parse("2026-05-25T01:00:00Z"),
                Instant.parse("2026-05-25T01:00:01Z"),
                Map.of("title", "Weekly sales summary")));
        ledger.save(new AgentRunRecord(
                "run-search-2",
                1L,
                "session-1",
                AgentRunStatus.FAILED,
                "Deploy app",
                "approval required",
                "trace-2",
                Instant.parse("2026-05-25T02:00:00Z"),
                Instant.parse("2026-05-25T02:00:01Z"),
                Map.of("title", "Deploy app")));
        ledger.save(new AgentRunRecord(
                "run-search-3",
                1L,
                "session-2",
                AgentRunStatus.SUCCEEDED,
                "Weekly report for another session",
                "done",
                "trace-3",
                Instant.parse("2026-05-25T03:00:00Z"),
                Instant.parse("2026-05-25T03:00:01Z"),
                Map.of("title", "Other weekly report")));

        List<AgentRunRecord> found = ledger.search(new AgentRunSearchQuery(
                "session-1",
                AgentRunStatus.SUCCEEDED,
                "weekly",
                10));

        assertEquals(1, found.size());
        assertEquals("run-search-1", found.getFirst().runId());
        assertEquals("Weekly sales summary", found.getFirst().metadata().get("title"));
        assertTrue(ledger.search(new AgentRunSearchQuery(null, null, null, 10)).isEmpty());
    }

    private static JdbcDataSource dataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:agent_run_" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        return dataSource;
    }

    private static void createSchema(JdbcDataSource dataSource) {
        new JdbcTemplate(dataSource).execute("""
                CREATE TABLE agent_run (
                  run_id VARCHAR(128) PRIMARY KEY,
                  agent_id BIGINT,
                  session_id VARCHAR(128),
                  status VARCHAR(32) NOT NULL,
                  input CLOB,
                  output CLOB,
                  trace_id VARCHAR(128),
                  started_at TIMESTAMP,
                  ended_at TIMESTAMP,
                  metadata_json CLOB,
                  created_at TIMESTAMP,
                  updated_at TIMESTAMP
                )
                """);
    }
}
