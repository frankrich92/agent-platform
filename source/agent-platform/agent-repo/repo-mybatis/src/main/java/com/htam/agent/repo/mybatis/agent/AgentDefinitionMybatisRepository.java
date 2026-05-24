package com.htam.agent.repo.mybatis.agent;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.htam.agent.repo.mybatis.agent.mapper.AgentDefinitionMapper;
import com.htam.agent.common.entity.AgentDefinition;
import com.htam.agent.common.enums.AgentType;
import com.htam.agent.common.mp.support.MP;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.repo.agent.AgentDefinitionRepository;
import com.htam.agent.repo.support.RepoPage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AgentDefinitionMybatisRepository implements AgentDefinitionRepository {
    private final AgentDefinitionMapper agentDefinitionMapper;

    @Override
    public RepoPage<AgentDefinition> page(PageParams pageParams,
                                          String name,
                                          AgentType agentType,
                                          String agentCode,
                                          String tag,
                                          Boolean enabled) {
        IPage<AgentDefinition> page = agentDefinitionMapper.selectPage(MP.getPage(pageParams),
                Wrappers.<AgentDefinition>lambdaQuery()
                        .like(name != null && !name.isBlank(), AgentDefinition::getName, name)
                        .eq(agentType != null, AgentDefinition::getAgentType, agentType)
                        .eq(agentCode != null && !agentCode.isBlank(), AgentDefinition::getAgentCode, agentCode)
                        .eq(tag != null && !tag.isBlank(), AgentDefinition::getTag, tag)
                        .eq(enabled != null, AgentDefinition::getEnabled, enabled));
        return new RepoPage<>(page.getRecords(), page.getTotal(), page.getSize(), page.getCurrent());
    }

    @Override
    public List<AgentDefinition> list() {
        return agentDefinitionMapper.selectList(null);
    }

    @Override
    public AgentDefinition getById(Long id) {
        return agentDefinitionMapper.selectById(id);
    }

    @Override
    public AgentDefinition getByAgentCode(String agentCode) {
        return agentDefinitionMapper.selectOne(Wrappers.<AgentDefinition>lambdaQuery()
                .eq(AgentDefinition::getAgentCode, agentCode));
    }

    @Override
    public boolean save(AgentDefinition entity) {
        return agentDefinitionMapper.insert(entity) > 0;
    }

    @Override
    public boolean updateById(AgentDefinition entity) {
        return agentDefinitionMapper.updateById(entity) > 0;
    }

    @Override
    public List<AgentDefinition> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return agentDefinitionMapper.selectBatchIds(ids);
    }

    @Override
    public List<AgentDefinition> listByModelConfigIds(List<Long> modelConfigIds) {
        if (modelConfigIds == null || modelConfigIds.isEmpty()) {
            return List.of();
        }
        return agentDefinitionMapper.selectList(Wrappers.<AgentDefinition>lambdaQuery()
                .in(AgentDefinition::getModelConfigId, modelConfigIds));
    }

    @Override
    public List<AgentDefinition> listBySensitiveWordConfigIds(List<Long> sensitiveWordConfigIds) {
        if (sensitiveWordConfigIds == null || sensitiveWordConfigIds.isEmpty()) {
            return List.of();
        }
        return agentDefinitionMapper.selectList(Wrappers.<AgentDefinition>lambdaQuery()
                .in(AgentDefinition::getSensitiveWordConfigId, sensitiveWordConfigIds));
    }

    @Override
    public List<AgentDefinition> listBySystemPromptTemplateIds(List<Long> systemPromptTemplateIds) {
        if (systemPromptTemplateIds == null || systemPromptTemplateIds.isEmpty()) {
            return List.of();
        }
        return agentDefinitionMapper.selectList(Wrappers.<AgentDefinition>lambdaQuery()
                .in(AgentDefinition::getSystemPromptTemplateId, systemPromptTemplateIds));
    }

    @Override
    public boolean deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }
        return agentDefinitionMapper.deleteBatchIds(ids) >= 0;
    }

    @Override
    public List<String> listTags() {
        return agentDefinitionMapper.selectList(Wrappers.<AgentDefinition>lambdaQuery()
                        .select(AgentDefinition::getTag)
                        .isNotNull(AgentDefinition::getTag)
                        .groupBy(AgentDefinition::getTag))
                .stream()
                .map(AgentDefinition::getTag)
                .filter(tag -> tag != null && !tag.isEmpty())
                .toList();
    }
}
