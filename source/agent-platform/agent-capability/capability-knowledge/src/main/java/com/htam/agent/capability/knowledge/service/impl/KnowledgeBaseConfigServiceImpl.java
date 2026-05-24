package com.htam.agent.capability.knowledge.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htam.agent.run.event.cluster.core.MessagePublisher;
import com.htam.agent.common.consts.RedisChannelTopic;
import com.htam.agent.common.dto.KnowledgeBaseConfigDTO;
import com.htam.agent.common.entity.KnowledgeBaseConfig;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.capability.knowledge.service.AgentKnowledgeBaseService;
import com.htam.agent.capability.knowledge.service.KnowledgeBaseConfigService;
import com.htam.agent.repo.agent.AgentDefinitionRepository;
import com.htam.agent.repo.knowledge.KnowledgeBaseConfigRepository;
import com.htam.agent.repo.support.RepoPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 知识库配置Service实现
 *
 * @author huxuehao
 */
@Service
@RequiredArgsConstructor
public class KnowledgeBaseConfigServiceImpl implements KnowledgeBaseConfigService {
    private final AgentDefinitionRepository agentDefinitionRepository;
    private final KnowledgeBaseConfigRepository knowledgeBaseConfigRepository;
    private final AgentKnowledgeBaseService agentKnowledgeBaseService;
    private final MessagePublisher messagePublisher;

    @Override
    public IPage<KnowledgeBaseConfig> page(PageParams pageParams, KnowledgeBaseConfigDTO query) {
        RepoPage<KnowledgeBaseConfig> repoPage = knowledgeBaseConfigRepository.page(
                pageParams,
                query.getName(),
                query.getKbType(),
                query.getEnabled());
        IPage<KnowledgeBaseConfig> page = new Page<>(repoPage.current(), repoPage.size(), repoPage.total());
        page.setRecords(repoPage.records());
        return page;
    }

    @Override
    public KnowledgeBaseConfig getById(Long id) {
        return knowledgeBaseConfigRepository.getById(id);
    }

    @Override
    public boolean save(KnowledgeBaseConfig entity) {
        return knowledgeBaseConfigRepository.save(entity);
    }

    @Override
    public List<Object> usedWithAgent(List<Long> ids) {
        List<Object> names = new ArrayList<>();
        agentDefinitionRepository.listByIds(agentKnowledgeBaseService.getAgentIds(ids)).forEach(agentDefinition -> {
            names.add(agentDefinition.getName());
        });

        return names;
    }

    @Override
    public KnowledgeBaseConfig getByAgentId(Long agentId) {
        List<Long> knowledgeIds = agentKnowledgeBaseService.getKnowledgeIds(agentId);

        if (knowledgeIds.isEmpty()) {
            return null;
        }

        List<KnowledgeBaseConfig> knowledgeBaseConfigs = knowledgeBaseConfigRepository.listByIds(knowledgeIds);
        if (knowledgeBaseConfigs == null) {
            return null;
        }

        return knowledgeBaseConfigs.getFirst();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByIds(List<Long> ids) {
        // 删除前先获取关联的智能体ID，以便后续触发重新注册
        List<Long> agentIds = agentKnowledgeBaseService.getAgentIds(ids);
        knowledgeBaseConfigRepository.deleteByIds(ids);
        boolean result = agentKnowledgeBaseService.deleteByKnowledgeIds(ids);
        publishAgentReregister(agentIds);
        return result;
    }

    @Override
    public boolean doUpdate(KnowledgeBaseConfig entity) {
        boolean result = knowledgeBaseConfigRepository.updateById(entity);
        publishAgentReregister(agentKnowledgeBaseService.getAgentIds(List.of(entity.getId())));
        return result;
    }

    private void publishAgentReregister(List<Long> agentIds) {
        agentIds.forEach(agentId ->
                messagePublisher.publish(RedisChannelTopic.AGENT_REREGISTER_CHANNEL, String.valueOf(agentId)));
    }

}
