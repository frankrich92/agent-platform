package com.htam.agent.run.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

class JdbcRuntimeEventLedgerConditionTest {

    @Test
    void jdbcEventLedgerIsSelectedByStoreProperty() {
        ConditionalOnProperty condition = JdbcRuntimeEventLedger.class.getAnnotation(ConditionalOnProperty.class);

        assertEquals("agent.run.ledger.event-store", condition.name()[0]);
        assertEquals("jdbc", condition.havingValue());
        assertTrue(condition.matchIfMissing());
    }

    @Test
    void r2dbcEventLedgerIsSelectedByStoreProperty() {
        ConditionalOnProperty condition = R2dbcRuntimeEventLedger.class.getAnnotation(ConditionalOnProperty.class);

        assertEquals("agent.run.ledger.event-store", condition.name()[0]);
        assertEquals("r2dbc", condition.havingValue());
        assertFalse(condition.matchIfMissing());
    }
}
