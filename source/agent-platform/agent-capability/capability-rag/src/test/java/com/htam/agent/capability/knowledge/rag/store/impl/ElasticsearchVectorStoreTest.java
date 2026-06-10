package com.htam.agent.capability.knowledge.rag.store.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class ElasticsearchVectorStoreTest {

    @Test
    void unavailableWithoutClientAndRejectsOperations() {
        ElasticsearchVectorStore store = new ElasticsearchVectorStore(null, new ObjectMapper(), "agent_rag");

        assertFalse(store.isAvailable());
        assertEquals("agent_rag_768", store.getIndexName(768));
        assertThrows(RuntimeException.class,
                () -> store.storeEmbedding(1L, 2L, 3L, 4L, new float[] {1.0f, 0.0f}));
    }
}
