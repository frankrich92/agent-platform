package com.htam.agent.repo.mybatis.run;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htam.agent.common.entity.AgentRun;
import com.htam.agent.repo.mybatis.run.mapper.AgentRunMapper;
import com.htam.agent.run.AgentRunLedger;
import com.htam.agent.run.AgentRunRecord;
import com.htam.agent.run.AgentRunSearchQuery;
import com.htam.agent.runtime.AgentRunStatus;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "agent.run.ledger.structured-store", havingValue = "mybatis", matchIfMissing = true)
@RequiredArgsConstructor
public class MybatisPlusAgentRunLedger implements AgentRunLedger {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};
    private static final ZoneId STORAGE_ZONE = ZoneId.systemDefault();

    private final AgentRunMapper agentRunMapper;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Override
    public AgentRunRecord save(AgentRunRecord record) {
        AgentRun entity = toEntity(record);
        entity.setUpdatedAt(LocalDateTime.now());
        if (agentRunMapper.selectById(record.runId()) == null) {
            entity.setCreatedAt(LocalDateTime.now());
            agentRunMapper.insert(entity);
        } else {
            agentRunMapper.updateById(entity);
        }
        return record;
    }

    @Override
    public Optional<AgentRunRecord> findById(String runId) {
        if (runId == null || runId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(agentRunMapper.selectById(runId)).map(this::toRecord);
    }

    @Override
    public List<AgentRunRecord> listBySessionId(String sessionId) {
        return search(AgentRunSearchQuery.bySessionId(sessionId));
    }

    @Override
    public List<AgentRunRecord> search(AgentRunSearchQuery query) {
        if (query == null || query.empty()) {
            return List.of();
        }
        LambdaQueryWrapper<AgentRun> wrapper = new LambdaQueryWrapper<AgentRun>()
                .eq(query.sessionId() != null, AgentRun::getSessionId, query.sessionId())
                .eq(query.status() != null, AgentRun::getStatus, query.status() == null ? null : query.status().name())
                .and(query.keyword() != null, nested -> nested
                        .like(AgentRun::getRunId, query.keyword())
                        .or()
                        .like(AgentRun::getInput, query.keyword())
                        .or()
                        .like(AgentRun::getOutput, query.keyword())
                        .or()
                        .like(AgentRun::getTraceId, query.keyword())
                        .or()
                        .like(AgentRun::getMetadataJson, query.keyword()))
                .orderByDesc(AgentRun::getStartedAt)
                .last("LIMIT " + query.limit());
        return agentRunMapper.selectList(wrapper).stream()
                .map(this::toRecord)
                .toList();
    }

    AgentRun toEntity(AgentRunRecord record) {
        AgentRun entity = new AgentRun();
        entity.setRunId(record.runId());
        entity.setAgentId(record.agentId());
        entity.setSessionId(record.sessionId());
        entity.setStatus(record.status().name());
        entity.setInput(record.input());
        entity.setOutput(record.output());
        entity.setTraceId(record.traceId());
        entity.setStartedAt(toLocalDateTime(record.startedAt()));
        entity.setEndedAt(toLocalDateTime(record.endedAt()));
        entity.setMetadataJson(json(record.metadata()));
        return entity;
    }

    AgentRunRecord toRecord(AgentRun entity) {
        return new AgentRunRecord(
                entity.getRunId(),
                entity.getAgentId(),
                entity.getSessionId(),
                enumValue(AgentRunStatus.class, entity.getStatus(), AgentRunStatus.ACCEPTED),
                entity.getInput(),
                entity.getOutput(),
                entity.getTraceId(),
                toInstant(entity.getStartedAt()),
                toInstant(entity.getEndedAt()),
                map(entity.getMetadataJson()));
    }

    private String json(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Map.of() : value);
        } catch (Exception e) {
            throw new IllegalStateException("运行元数据序列化失败", e);
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

    private static LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, STORAGE_ZONE);
    }

    private static Instant toInstant(LocalDateTime time) {
        return time == null ? null : time.atZone(STORAGE_ZONE).toInstant();
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
