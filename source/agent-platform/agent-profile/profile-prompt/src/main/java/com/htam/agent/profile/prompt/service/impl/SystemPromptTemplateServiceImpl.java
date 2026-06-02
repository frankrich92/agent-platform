package com.htam.agent.profile.prompt.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htam.agent.run.event.cluster.core.MessagePublisher;
import com.htam.agent.common.consts.RedisChannelTopic;
import com.htam.agent.common.dto.SystemPromptTemplateDTO;
import com.htam.agent.common.entity.AgentDefinition;
import com.htam.agent.common.entity.SystemPromptTemplate;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.profile.prompt.service.SystemPromptTemplateService;
import com.htam.agent.repo.agent.AgentDefinitionRepository;
import com.htam.agent.repo.capability.SystemPromptTemplateRepository;
import com.htam.agent.repo.support.RepoPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 系统提示词模板Service实现
 *
 * @author huxuehao
 */
@Service
@RequiredArgsConstructor
public class SystemPromptTemplateServiceImpl implements SystemPromptTemplateService {
    private final AgentDefinitionRepository agentDefinitionRepository;
    private final SystemPromptTemplateRepository systemPromptTemplateRepository;
    private final MessagePublisher messagePublisher;

    @Override
    public IPage<SystemPromptTemplate> page(PageParams pageParams, SystemPromptTemplateDTO query) {
        SystemPromptTemplateDTO templateQuery = query == null ? new SystemPromptTemplateDTO() : query;
        RepoPage<SystemPromptTemplate> repoPage = systemPromptTemplateRepository.page(
                pageParams,
                templateQuery.getCategory(),
                templateQuery.getName(),
                templateQuery.getEnabled());
        IPage<SystemPromptTemplate> page = new Page<>(repoPage.current(), repoPage.size(), repoPage.total());
        page.setRecords(repoPage.records());
        return page;
    }

    @Override
    public SystemPromptTemplate getById(Long id) {
        return systemPromptTemplateRepository.getById(id);
    }

    @Override
    public boolean save(SystemPromptTemplate entity) {
        return systemPromptTemplateRepository.save(entity);
    }

    @Override
    public List<Object> usedWithAgent(List<Long> ids) {
        ArrayList<Object> names = new ArrayList<>();
        getAgentDefinitions(ids).forEach(agentDefinition -> {
            names.add(agentDefinition.getName());
        });

        return names;
    }

    @Override
    public List<String> listCategories() {
        return systemPromptTemplateRepository.listCategories();
    }

    @Override
    public boolean deleteByIds(List<Long> ids) {
        // 删除前先获取关联的智能体ID，以便后续触发重新注册
        List<Long> agentIds = getAgentDefinitions(ids).stream().map(AgentDefinition::getId).toList();
        boolean result = systemPromptTemplateRepository.deleteByIds(ids);
        publishAgentReregister(agentIds);
        return result;
    }

    @Override
    public boolean doUpdate(SystemPromptTemplate entity) {
        boolean result = systemPromptTemplateRepository.updateById(entity);
        List<Long> agentIds = getAgentDefinitions(List.of(entity.getId())).stream().map(AgentDefinition::getId).toList();
        publishAgentReregister(agentIds);
        return result;
    }

    private void publishAgentReregister(List<Long> agentIds) {
        agentIds.forEach(agentId ->
                messagePublisher.publishAfterCommit(RedisChannelTopic.AGENT_REREGISTER_CHANNEL, String.valueOf(agentId)));
    }

    private List<AgentDefinition> getAgentDefinitions(List<Long> systemPromptId) {
        if (systemPromptId == null || systemPromptId.isEmpty()) {
            return new ArrayList<>();
        }

        return agentDefinitionRepository.listBySystemPromptTemplateIds(systemPromptId);
    }
}
