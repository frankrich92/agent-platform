package com.htam.agent.governance.sensitive.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htam.agent.cluster.core.MessagePublisher;
import com.htam.agent.common.consts.RedisChannelTopic;
import com.htam.agent.common.dto.SensitiveWordConfigDTO;
import com.htam.agent.common.entity.AgentDefinition;
import com.htam.agent.common.entity.SensitiveWordConfig;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.repo.agent.AgentDefinitionRepository;
import com.htam.agent.repo.capability.SensitiveWordConfigRepository;
import com.htam.agent.repo.support.RepoPage;
import com.htam.agent.governance.sensitive.service.SensitiveWordConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 敏感词配置Service实现
 *
 * @author huxuehao
 */
@Service
@RequiredArgsConstructor
public class SensitiveWordConfigServiceImpl implements SensitiveWordConfigService {
    private final AgentDefinitionRepository agentDefinitionRepository;
    private final SensitiveWordConfigRepository sensitiveWordConfigRepository;
    private final MessagePublisher messagePublisher;

    @Override
    public IPage<SensitiveWordConfig> page(PageParams pageParams, SensitiveWordConfigDTO query) {
        SensitiveWordConfigDTO configQuery = query == null ? new SensitiveWordConfigDTO() : query;
        RepoPage<SensitiveWordConfig> repoPage = sensitiveWordConfigRepository.page(
                pageParams,
                configQuery.getCategory(),
                configQuery.getName(),
                configQuery.getEnabled());
        IPage<SensitiveWordConfig> page = new Page<>(repoPage.current(), repoPage.size(), repoPage.total());
        page.setRecords(repoPage.records());
        return page;
    }

    @Override
    public SensitiveWordConfig getById(Long id) {
        return sensitiveWordConfigRepository.getById(id);
    }

    @Override
    public boolean save(SensitiveWordConfig entity) {
        return sensitiveWordConfigRepository.save(entity);
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
        return sensitiveWordConfigRepository.listCategories();
    }

    @Override
    public boolean deleteByIds(List<Long> ids) {
        // 删除前先获取关联的智能体ID，以便后续触发重新注册
        List<Long> agentIds = getAgentDefinitions(ids).stream().map(AgentDefinition::getId).toList();
        boolean result = sensitiveWordConfigRepository.deleteByIds(ids);
        publishAgentReregister(agentIds);
        return result;
    }

    @Override
    public boolean doUpdate(SensitiveWordConfig entity) {
        boolean result = sensitiveWordConfigRepository.updateById(entity);
        List<Long> agentIds = getAgentDefinitions(List.of(entity.getId())).stream().map(AgentDefinition::getId).toList();
        publishAgentReregister(agentIds);
        return result;
    }

    private void publishAgentReregister(List<Long> agentIds) {
        agentIds.forEach(agentId ->
                messagePublisher.publish(RedisChannelTopic.AGENT_REREGISTER_CHANNEL, String.valueOf(agentId)));
    }

    private List<AgentDefinition> getAgentDefinitions(List<Long> sensitiveWordConfigIds) {
        if (sensitiveWordConfigIds == null || sensitiveWordConfigIds.isEmpty()) {
            return new ArrayList<>();
        }
        return agentDefinitionRepository.listBySensitiveWordConfigIds(sensitiveWordConfigIds);
    }
}
