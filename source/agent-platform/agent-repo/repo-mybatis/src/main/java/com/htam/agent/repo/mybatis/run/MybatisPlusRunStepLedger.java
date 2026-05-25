package com.htam.agent.repo.mybatis.run;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htam.agent.common.entity.AgentRunStep;
import com.htam.agent.repo.mybatis.run.mapper.AgentRunStepMapper;
import com.htam.agent.run.step.RunStepLedger;
import com.htam.agent.runtime.AgentRunStatus;
import com.htam.agent.runtime.RunStep;
import com.htam.agent.runtime.RunStepType;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "agent.run.ledger.structured-store", havingValue = "mybatis", matchIfMissing = true)
@RequiredArgsConstructor
public class MybatisPlusRunStepLedger implements RunStepLedger {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};
    private static final ZoneId STORAGE_ZONE = ZoneId.systemDefault();

    private final AgentRunStepMapper agentRunStepMapper;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Override
    public void append(RunStep step) {
        if (step == null) {
            return;
        }
        AgentRunStep entity = toEntity(step);
        if (agentRunStepMapper.selectById(step.stepId()) == null) {
            agentRunStepMapper.insert(entity);
        } else {
            agentRunStepMapper.updateById(entity);
        }
    }

    @Override
    public List<RunStep> listByRunId(String runId) {
        if (runId == null || runId.isBlank()) {
            return List.of();
        }
        return agentRunStepMapper.selectList(new LambdaQueryWrapper<AgentRunStep>()
                        .eq(AgentRunStep::getRunId, runId)
                        .orderByAsc(AgentRunStep::getStartedAt)
                        .orderByAsc(AgentRunStep::getStepId))
                .stream()
                .map(this::toRecord)
                .toList();
    }

    AgentRunStep toEntity(RunStep step) {
        AgentRunStep entity = new AgentRunStep();
        entity.setStepId(step.stepId());
        entity.setRunId(step.runId());
        entity.setStepType(step.stepType().name());
        entity.setStatus(step.status().name());
        entity.setStartedAt(toLocalDateTime(step.startedAt()));
        entity.setEndedAt(toLocalDateTime(step.endedAt()));
        entity.setSummaryJson(json(step.summary()));
        return entity;
    }

    RunStep toRecord(AgentRunStep entity) {
        return new RunStep(
                entity.getStepId(),
                entity.getRunId(),
                enumValue(RunStepType.class, entity.getStepType(), RunStepType.OTHER),
                enumValue(AgentRunStatus.class, entity.getStatus(), AgentRunStatus.ACCEPTED),
                toInstant(entity.getStartedAt()),
                toInstant(entity.getEndedAt()),
                map(entity.getSummaryJson()));
    }

    private String json(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Map.of() : value);
        } catch (Exception e) {
            throw new IllegalStateException("运行步骤序列化失败", e);
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
