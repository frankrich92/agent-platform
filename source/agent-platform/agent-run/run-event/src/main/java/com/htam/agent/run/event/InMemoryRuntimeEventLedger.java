package com.htam.agent.run.event;

import com.htam.agent.runtime.RuntimeEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;

@Component
public class InMemoryRuntimeEventLedger implements RuntimeEventLedger {

    private final ConcurrentMap<String, List<RuntimeEvent>> eventsByRunId = new ConcurrentHashMap<>();

    @Override
    public void append(RuntimeEvent event) {
        if (event == null) {
            return;
        }
        eventsByRunId.compute(event.runId(), (runId, existing) -> {
            List<RuntimeEvent> events = existing == null ? new ArrayList<>() : new ArrayList<>(existing);
            events.add(event);
            events.sort(Comparator.comparingLong(RuntimeEvent::sequence));
            return List.copyOf(events);
        });
    }

    @Override
    public List<RuntimeEvent> listByRunId(String runId) {
        if (runId == null || runId.isBlank()) {
            return List.of();
        }
        return eventsByRunId.getOrDefault(runId, List.of());
    }
}
