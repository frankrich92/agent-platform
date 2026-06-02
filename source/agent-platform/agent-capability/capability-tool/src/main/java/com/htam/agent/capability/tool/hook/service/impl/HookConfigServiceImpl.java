package com.htam.agent.capability.tool.hook.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htam.agent.run.event.cluster.core.MessagePublisher;
import com.htam.agent.common.consts.RedisChannelTopic;
import com.htam.agent.common.dto.HookConfigDTO;
import com.htam.agent.common.entity.AgentDefinition;
import com.htam.agent.common.entity.HookConfig;
import com.htam.agent.common.enums.HookType;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.common.wrapper.HookConfigWrapper;
import com.htam.agent.capability.tool.hook.service.AgentHookService;
import com.htam.agent.capability.tool.hook.service.HookConfigService;
import com.htam.agent.repo.agent.AgentDefinitionRepository;
import com.htam.agent.repo.capability.HookConfigRepository;
import com.htam.agent.repo.support.RepoPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Hook配置Service实现
 *
 * @author huxuehao
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HookConfigServiceImpl implements HookConfigService {
    private final AgentDefinitionRepository agentDefinitionRepository;
    private final HookConfigRepository hookConfigRepository;
    private final AgentHookService agentHookService;
    private final MessagePublisher messagePublisher;

    @Override
    public IPage<HookConfig> page(PageParams pageParams, HookConfigDTO query) {
        HookConfigDTO hookQuery = query == null ? new HookConfigDTO() : query;
        RepoPage<HookConfig> repoPage = hookConfigRepository.page(
                pageParams,
                hookQuery.getName(),
                hookQuery.getHookType(),
                hookQuery.getEnabled());
        IPage<HookConfig> page = new Page<>(repoPage.current(), repoPage.size(), repoPage.total());
        page.setRecords(repoPage.records());
        return page;
    }

    @Override
    public HookConfig getById(Long id) {
        return hookConfigRepository.getById(id);
    }

    @Override
    public List<HookConfig> listByIds(List<Long> ids) {
        return hookConfigRepository.listByIds(ids);
    }

    @Override
    public boolean save(HookConfig entity) {
        return hookConfigRepository.save(entity);
    }

    @Override
    public void SyncConfigToDatabase(List<HookConfigWrapper> configWrappers) {
        hookConfigRepository.deleteBuiltinNotInClassPaths(configWrappers.stream()
                .map(HookConfigWrapper::getClassPath)
                .toList());
        configWrappers.forEach(configWrapper -> {
            List<HookConfig> list = hookConfigRepository.listByClassPath(configWrapper.getClassPath());
            if (list.isEmpty()) {
                HookConfig hookConfig = new HookConfig();
                hookConfig.setName(configWrapper.getName());
                hookConfig.setHookType(HookType.BUILTIN);
                hookConfig.setDescription(configWrapper.getDescription());
                hookConfig.setClassPath(configWrapper.getClassPath());
                hookConfig.setEnabled(true);
                hookConfig.setPriority(1);
                hookConfigRepository.save(hookConfig);
            } else {
                for (int i = 0; i < list.size(); i++) {
                    if (i == 0) {
                        list.get(i).setHookType(HookType.BUILTIN);
                        list.get(i).setClassPath(configWrapper.getClassPath());
                        hookConfigRepository.updateById(list.get(i));
                    } else {
                        hookConfigRepository.deleteById(list.get(i).getId());
                    }
                }
            }
        });
    }

    @Override
    public List<Object> usedWithAgent(List<Long> ids) {
        List<Object> names = new ArrayList<>();
        agentDefinitionRepository.listByIds(agentHookService.getAgentIds(ids)).forEach(agentDefinition -> {
            names.add(agentDefinition.getName());
        });

        return names;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByIds(List<Long> ids) {
        // 删除前先获取关联的智能体ID，以便后续触发重新注册
        List<Long> agentIds = agentHookService.getAgentIds(ids);
        boolean result = hookConfigRepository.deleteByIds(ids);
        agentHookService.deleteByHookConfigIds(ids);
        publishAgentReregister(agentIds);
        return result;
    }

    @Override
    public boolean doUpdate(HookConfig entity) {
        boolean result = hookConfigRepository.updateById(entity);
        publishAgentReregister(agentHookService.getAgentIds(List.of(entity.getId())));
        return result;
    }

    private void publishAgentReregister(List<Long> agentIds) {
        agentIds.forEach(agentId ->
                messagePublisher.publishAfterCommit(RedisChannelTopic.AGENT_REREGISTER_CHANNEL, String.valueOf(agentId)));
    }

}
