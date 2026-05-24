package com.htam.agent.capability.provider.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htam.agent.cluster.core.MessagePublisher;
import com.htam.agent.common.consts.RedisChannelTopic;
import com.htam.agent.common.dto.ModelProviderDTO;
import com.htam.agent.common.entity.ModelConfig;
import com.htam.agent.common.entity.ModelProvider;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.capability.provider.service.ModelProviderService;
import com.htam.agent.repo.agent.AgentDefinitionRepository;
import com.htam.agent.repo.provider.ModelConfigRepository;
import com.htam.agent.repo.provider.ModelProviderRepository;
import com.htam.agent.repo.support.RepoPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 模型提供商Service实现
 *
 * @author huxuehao
 **/
@Service
@RequiredArgsConstructor
public class ModelProviderServiceImpl implements ModelProviderService {
    private final AgentDefinitionRepository agentDefinitionRepository;
    private final ModelProviderRepository modelProviderRepository;
    private final ModelConfigRepository modelConfigRepository;
    private final MessagePublisher messagePublisher;

    @Override
    public IPage<ModelProvider> page(PageParams pageParams, ModelProviderDTO query) {
        ModelProviderDTO providerQuery = query == null ? new ModelProviderDTO() : query;
        RepoPage<ModelProvider> repoPage = modelProviderRepository.page(
                pageParams,
                providerQuery.getName(),
                providerQuery.getType(),
                providerQuery.getEnabled());
        IPage<ModelProvider> page = new Page<>(repoPage.current(), repoPage.size(), repoPage.total());
        page.setRecords(repoPage.records());
        return page;
    }

    @Override
    public ModelProvider getById(Long id) {
        return modelProviderRepository.getById(id);
    }

    @Override
    public boolean save(ModelProvider entity) {
        return modelProviderRepository.save(entity);
    }

    @Override
    public List<Object> usedWithModel(List<Long> ids) {
        List<Object> names = new ArrayList<>();

        modelConfigRepository.listByProviderIds(ids).forEach(modelConfig -> {
            names.add(modelConfig.getName());
        });

        return names;
    }

    @Override
    public boolean deleteByIds(List<Long> ids) {
        // 删除前先获取关联的智能体ID（两级：供应商→模型配置→智能体）
        List<Long> agentIds = getAgentIdsByProviderIds(ids);
        boolean result = modelProviderRepository.deleteByIds(ids);
        publishAgentReregister(agentIds);
        return result;
    }

    @Override
    public boolean doUpdate(ModelProvider entity) {
        boolean result = modelProviderRepository.updateById(entity);
        List<Long> agentIds = getAgentIdsByProviderIds(List.of(entity.getId()));
        publishAgentReregister(agentIds);
        return result;
    }

    /**
     * 两级查询：供应商ID → 模型配置ID → 智能体ID
     *
     * @param providerIds 供应商ID列表
     * @return 关联的智能体ID列表
     */
    private List<Long> getAgentIdsByProviderIds(List<Long> providerIds) {
        if (providerIds == null || providerIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> modelConfigIds = modelConfigRepository.listByProviderIds(providerIds)
                .stream()
                .map(ModelConfig::getId)
                .toList();

        if (modelConfigIds.isEmpty()) {
            return new ArrayList<>();
        }

        return agentDefinitionRepository.listByModelConfigIds(modelConfigIds)
                .stream()
                .map(agentDefinition -> agentDefinition.getId())
                .toList();
    }

    private void publishAgentReregister(List<Long> agentIds) {
        agentIds.forEach(agentId ->
                messagePublisher.publish(RedisChannelTopic.AGENT_REREGISTER_CHANNEL, String.valueOf(agentId)));
    }
}
