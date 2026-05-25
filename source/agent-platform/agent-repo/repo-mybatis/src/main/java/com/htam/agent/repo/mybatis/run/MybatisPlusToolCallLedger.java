package com.htam.agent.repo.mybatis.run;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htam.agent.common.entity.AgentToolCall;
import com.htam.agent.repo.mybatis.run.mapper.AgentToolCallMapper;
import com.htam.agent.run.toolcall.ToolCallLedger;
import com.htam.agent.runtime.ToolCall;
import com.htam.agent.runtime.ToolCallPolicy;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "agent.run.ledger.structured-store", havingValue = "mybatis", matchIfMissing = true)
@RequiredArgsConstructor
public class MybatisPlusToolCallLedger implements ToolCallLedger {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final AgentToolCallMapper agentToolCallMapper;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Override
    public void append(ToolCall toolCall) {
        if (toolCall == null) {
            return;
        }
        AgentToolCall entity = toEntity(toolCall);
        if (agentToolCallMapper.selectById(toolCall.toolCallId()) == null) {
            entity.setCreatedAt(LocalDateTime.now());
            agentToolCallMapper.insert(entity);
        } else {
            agentToolCallMapper.updateById(entity);
        }
    }

    @Override
    public List<ToolCall> listByRunId(String runId) {
        if (runId == null || runId.isBlank()) {
            return List.of();
        }
        return agentToolCallMapper.selectList(new LambdaQueryWrapper<AgentToolCall>()
                        .eq(AgentToolCall::getRunId, runId)
                        .orderByAsc(AgentToolCall::getCreatedAt)
                        .orderByAsc(AgentToolCall::getToolCallId))
                .stream()
                .map(this::toRecord)
                .toList();
    }

    AgentToolCall toEntity(ToolCall toolCall) {
        AgentToolCall entity = new AgentToolCall();
        entity.setToolCallId(toolCall.toolCallId());
        entity.setRunId(toolCall.runId());
        entity.setToolName(toolCall.toolName());
        entity.setPolicy(toolCall.policy().name());
        entity.setReadOnly(toolCall.readOnly());
        entity.setDurationMillis(toolCall.duration().toMillis());
        entity.setCostMicros(toolCall.costMicros());
        entity.setParameterSummary(toolCall.parameterSummary());
        entity.setResultSummary(toolCall.resultSummary());
        entity.setErrorCode(toolCall.errorCode());
        entity.setErrorMessage(toolCall.errorMessage());
        entity.setAuditTagsJson(json(toolCall.auditTags()));
        return entity;
    }

    ToolCall toRecord(AgentToolCall entity) {
        return new ToolCall(
                entity.getToolCallId(),
                entity.getRunId(),
                entity.getToolName(),
                enumValue(ToolCallPolicy.class, entity.getPolicy(), ToolCallPolicy.ASK),
                Boolean.TRUE.equals(entity.getReadOnly()),
                Duration.ofMillis(entity.getDurationMillis() == null ? 0L : entity.getDurationMillis()),
                entity.getCostMicros() == null ? 0L : entity.getCostMicros(),
                entity.getParameterSummary(),
                entity.getResultSummary(),
                entity.getErrorCode(),
                entity.getErrorMessage(),
                map(entity.getAuditTagsJson()));
    }

    private String json(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Map.of() : value);
        } catch (Exception e) {
            throw new IllegalStateException("工具调用审计标签序列化失败", e);
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
