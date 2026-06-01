package com.htam.agent.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class AgentMetadataStoreTest {

    @AfterEach
    void tearDown() {
        AgentMetadataStore.clear();
    }

    @Test
    void separatesObjectOwnersByIdentity() {
        AgentOwner first = new AgentOwner("same-agent-id");
        AgentOwner second = new AgentOwner("same-agent-id");

        AgentMetadataStore.put(first, "threadId", "thread-1");
        AgentMetadataStore.put(second, "threadId", "thread-2");

        assertEquals("thread-1", AgentMetadataStore.get(first, "threadId"));
        assertEquals("thread-2", AgentMetadataStore.get(second, "threadId"));
        assertEquals(2, AgentMetadataStore.size());
    }

    @Test
    void keepsScalarOwnerCompatibility() {
        AgentMetadataStore.put("agent-id", "threadId", "thread-1");
        AgentMetadataStore.put("agent-id", "workspace", "workspace-1");

        assertEquals("thread-1", AgentMetadataStore.get("agent-id", "threadId"));
        assertEquals("workspace-1", AgentMetadataStore.get("agent-id", "workspace"));
        assertEquals(1, AgentMetadataStore.size());
    }

    @Test
    void removeIfReceivesOriginalObjectOwner() {
        AgentOwner owner = new AgentOwner("agent-id");
        AtomicReference<Object> seenOwner = new AtomicReference<>();

        AgentMetadataStore.put(owner, "threadId", "thread-1");
        int removedCount = AgentMetadataStore.removeIf((agent, metadata) -> {
            seenOwner.set(agent);
            return "thread-1".equals(metadata.get("threadId"));
        });

        assertEquals(1, removedCount);
        assertEquals(0, AgentMetadataStore.size());
        assertSame(owner, seenOwner.get());
    }

    private record AgentOwner(String agentId) {
    }
}
