package com.htam.agent.capability.knowledge.rag.store.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class WeaviateVectorStoreTest {

    @Test
    void unavailableWithoutClientAndUsesStableNames() {
        WeaviateVectorStore store = new WeaviateVectorStore(null, "AgentRag");

        assertFalse(store.isAvailable());
        assertEquals("AgentRag_1024", store.getClassName(1024));
        assertEquals(store.toObjectId(123L), store.toObjectId(123L));
        assertThrows(RuntimeException.class,
                () -> store.storeEmbedding(1L, 2L, 3L, 4L, new float[] {1.0f, 0.0f}));
    }
}
