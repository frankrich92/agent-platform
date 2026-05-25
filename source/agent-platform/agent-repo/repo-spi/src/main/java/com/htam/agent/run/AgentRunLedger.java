package com.htam.agent.run;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface AgentRunLedger {

    AgentRunRecord save(AgentRunRecord record);

    Optional<AgentRunRecord> findById(String runId);

    List<AgentRunRecord> listBySessionId(String sessionId);

    default List<AgentRunRecord> search(AgentRunSearchQuery query) {
        if (query == null || query.empty()) {
            return List.of();
        }
        Stream<AgentRunRecord> stream = query.sessionId() == null
                ? Stream.empty()
                : listBySessionId(query.sessionId()).stream();
        if (query.status() != null) {
            stream = stream.filter(record -> record.status() == query.status());
        }
        if (query.keyword() != null) {
            String keyword = query.keyword().toLowerCase();
            stream = stream.filter(record -> matches(record, keyword));
        }
        return stream.limit(query.limit()).toList();
    }

    private static boolean matches(AgentRunRecord record, String keyword) {
        return contains(record.runId(), keyword)
                || contains(record.input(), keyword)
                || contains(record.output(), keyword)
                || contains(record.traceId(), keyword)
                || contains(String.valueOf(record.metadata()), keyword);
    }

    private static boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }
}
