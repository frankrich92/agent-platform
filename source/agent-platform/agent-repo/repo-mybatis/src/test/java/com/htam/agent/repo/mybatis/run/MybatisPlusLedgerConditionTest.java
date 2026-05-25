package com.htam.agent.repo.mybatis.run;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

class MybatisPlusLedgerConditionTest {

    @Test
    void structuredLedgersAreSelectedByStoreProperty() {
        assertStructuredStoreCondition(MybatisPlusAgentRunLedger.class);
        assertStructuredStoreCondition(MybatisPlusRunStepLedger.class);
        assertStructuredStoreCondition(MybatisPlusToolCallLedger.class);
    }

    private static void assertStructuredStoreCondition(Class<?> type) {
        ConditionalOnProperty condition = type.getAnnotation(ConditionalOnProperty.class);

        assertEquals("agent.run.ledger.structured-store", condition.name()[0]);
        assertEquals("mybatis", condition.havingValue());
        assertTrue(condition.matchIfMissing());
    }
}
