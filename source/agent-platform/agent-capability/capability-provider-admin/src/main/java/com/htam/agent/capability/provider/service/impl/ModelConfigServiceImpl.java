package com.htam.agent.capability.provider.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htam.agent.cluster.core.MessagePublisher;
import com.htam.agent.common.consts.RedisChannelTopic;
import com.htam.agent.common.dto.ModelConfigDTO;
import com.htam.agent.common.entity.AgentDefinition;
import com.htam.agent.common.entity.ModelConfig;
import com.htam.agent.common.entity.ModelProvider;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.common.wrapper.ModelWrapper;
import com.htam.agent.capability.provider.service.ModelConfigService;
import com.htam.agent.capability.provider.service.ModelProviderService;
import com.htam.agent.repo.agent.AgentDefinitionRepository;
import com.htam.agent.repo.provider.ModelConfigRepository;
import com.htam.agent.repo.support.RepoPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 模型配置Service实现
 *
 * @author huxuehao
 */
@Service
@RequiredArgsConstructor
public class ModelConfigServiceImpl implements ModelConfigService {
    private final AgentDefinitionRepository agentDefinitionRepository;
    private final ModelConfigRepository modelConfigRepository;
    private final ModelProviderService modelProviderService;
    private final MessagePublisher messagePublisher;

    @Override
    public IPage<ModelConfig> page(PageParams pageParams, ModelConfigDTO query) {
        ModelConfigDTO configQuery = query == null ? new ModelConfigDTO() : query;
        RepoPage<ModelConfig> repoPage = modelConfigRepository.page(
                pageParams,
                configQuery.getProviderId(),
                configQuery.getName(),
                configQuery.getEnabled());
        IPage<ModelConfig> page = new Page<>(repoPage.current(), repoPage.size(), repoPage.total());
        page.setRecords(repoPage.records());
        return page;
    }

    @Override
    public ModelConfig getById(Long id) {
        return modelConfigRepository.getById(id);
    }

    @Override
    public boolean save(ModelConfig entity) {
        return modelConfigRepository.save(entity);
    }

    @Override
    public ModelWrapper getModelWrapperById(Long id) {
        ModelConfig config = getById(id);
        if (config == null) {
            throw new RuntimeException("model config not found");
        }

        if (!config.getEnabled()) {
            throw new RuntimeException("model config is disabled");
        }

        ModelProvider modelProvider = modelProviderService.getById(config.getProviderId());
        if (modelProvider == null) {
            throw new RuntimeException("model provider not found");
        }

        if (!modelProvider.getEnabled()) {
            throw new RuntimeException("model provider is disabled");
        }

        return ModelWrapper.builder()
                .config(config)
                .provider(modelProvider)
                .build();
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
    public boolean deleteByIds(List<Long> ids) {
        // 删除前先获取关联的智能体ID，以便后续触发重新注册
        List<Long> agentIds = getAgentDefinitions(ids).stream().map(AgentDefinition::getId).toList();
        boolean result = modelConfigRepository.deleteByIds(ids);
        publishAgentReregister(agentIds);
        return result;
    }

    @Override
    public boolean doUpdate(ModelConfig entity) {
        boolean result = modelConfigRepository.updateById(entity);
        List<Long> agentIds = getAgentDefinitions(List.of(entity.getId())).stream().map(AgentDefinition::getId).toList();
        publishAgentReregister(agentIds);
        return result;
    }

    private void publishAgentReregister(List<Long> agentIds) {
        agentIds.forEach(agentId ->
                messagePublisher.publish(RedisChannelTopic.AGENT_REREGISTER_CHANNEL, String.valueOf(agentId)));
    }

    private List<AgentDefinition> getAgentDefinitions(List<Long> modelConfigIds) {
        if (modelConfigIds == null || modelConfigIds.isEmpty()) {
            return new ArrayList<>();
        }
        return agentDefinitionRepository.listByModelConfigIds(modelConfigIds);
    }
}
