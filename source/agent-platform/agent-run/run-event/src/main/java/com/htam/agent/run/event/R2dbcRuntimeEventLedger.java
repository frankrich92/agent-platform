package com.htam.agent.run.event;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htam.agent.runtime.RuntimeEvent;
import com.htam.agent.runtime.RuntimeEventType;
import io.r2dbc.spi.Row;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@ConditionalOnProperty(name = "agent.run.ledger.event-store", havingValue = "r2dbc")
public class R2dbcRuntimeEventLedger implements RuntimeEventLedger {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};
    private static final ZoneId STORAGE_ZONE = ZoneId.systemDefault();

    private final DatabaseClient databaseClient;
    private final ObjectMapper objectMapper;

    public R2dbcRuntimeEventLedger(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Override
    public void append(RuntimeEvent event) {
        if (event == null) {
            return;
        }
        bindEvent(databaseClient.sql(
                                "INSERT INTO agent_run_event"
                                        + " (event_id, run_id, session_id, step_id, trace_id, sequence, event_type,"
                                        + " occurred_at, payload_json)"
                                        + " VALUES (:eventId, :runId, :sessionId, :stepId, :traceId, :sequence,"
                                        + " :eventType, :occurredAt, :payloadJson)"
                                        + " ON CONFLICT DO NOTHING"),
                        event)
                .fetch()
                .rowsUpdated()
                .onErrorResume(DuplicateKeyException.class, ignored -> Mono.empty())
                .block();
    }

    @Override
    public List<RuntimeEvent> listByRunId(String runId) {
        if (runId == null || runId.isBlank()) {
            return List.of();
        }
        return databaseClient.sql(
                        "SELECT event_id, event_type, run_id, session_id, step_id, trace_id, sequence,"
                                + " occurred_at, payload_json FROM agent_run_event"
                                + " WHERE run_id = :runId ORDER BY sequence, occurred_at, event_id")
                .bind("runId", runId)
                .map((row, metadata) -> mapRow(row))
                .all()
                .collectList()
                .blockOptional()
                .orElse(List.of());
    }

    private DatabaseClient.GenericExecuteSpec bindEvent(
            DatabaseClient.GenericExecuteSpec spec, RuntimeEvent event) {
        spec = bind(spec, "eventId", event.eventId(), String.class);
        spec = bind(spec, "runId", event.runId(), String.class);
        spec = bind(spec, "sessionId", event.sessionId(), String.class);
        spec = bind(spec, "stepId", event.stepId(), String.class);
        spec = bind(spec, "traceId", event.traceId(), String.class);
        spec = bind(spec, "sequence", event.sequence(), Long.class);
        spec = bind(spec, "eventType", event.eventType().name(), String.class);
        spec = bind(spec, "occurredAt", toLocalDateTime(event.timestamp()), LocalDateTime.class);
        return bind(spec, "payloadJson", json(event.payload()), String.class);
    }

    private static DatabaseClient.GenericExecuteSpec bind(
            DatabaseClient.GenericExecuteSpec spec, String name, Object value, Class<?> valueType) {
        return value == null ? spec.bindNull(name, valueType) : spec.bind(name, value);
    }

    private RuntimeEvent mapRow(Row row) {
        return new RuntimeEvent(
                string(row, "event_id"),
                enumValue(RuntimeEventType.class, string(row, "event_type"), RuntimeEventType.UNKNOWN),
                string(row, "run_id"),
                string(row, "session_id"),
                string(row, "step_id"),
                string(row, "trace_id"),
                longValue(row, "sequence"),
                instant(row, "occurred_at"),
                map(string(row, "payload_json")));
    }

    private String json(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Map.of() : value);
        } catch (Exception e) {
            throw new IllegalStateException("运行事件序列化失败", e);
        }
    }

    private Map<String, Object> map(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (Exception e) {
            return Map.of("raw", json);
        }
    }

    private static String string(Row row, String column) {
        Object value = row.get(column);
        return value == null ? null : value.toString();
    }

    private static long longValue(Row row, String column) {
        Object value = row.get(column);
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Long.parseLong(text);
        }
        return 0L;
    }

    private static LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, STORAGE_ZONE);
    }

    private static Instant instant(Row row, String column) {
        Object value = row.get(column);
        if (value instanceof Instant instant) {
            return instant;
        }
        if (value instanceof LocalDateTime time) {
            return time.atZone(STORAGE_ZONE).toInstant();
        }
        if (value instanceof OffsetDateTime time) {
            return time.toInstant();
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toInstant();
        }
        return null;
    }

    private static <E extends Enum<E>> E enumValue(Class<E> type, String value, E fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }
}
