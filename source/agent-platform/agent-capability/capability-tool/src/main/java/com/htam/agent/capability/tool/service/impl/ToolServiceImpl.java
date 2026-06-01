package com.htam.agent.capability.tool.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htam.agent.run.event.cluster.core.MessagePublisher;
import com.htam.agent.common.consts.RedisChannelTopic;
import com.htam.agent.common.dto.ToolDTO;
import com.htam.agent.common.entity.ToolConfig;
import com.htam.agent.common.enums.ToolType;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.common.util.JsonUtils;
import com.htam.agent.common.wrapper.ToolInfoWrapper;
import com.htam.agent.repo.agent.AgentDefinitionRepository;
import com.htam.agent.repo.capability.ToolConfigRepository;
import com.htam.agent.repo.support.RepoPage;
import com.htam.agent.capability.skill.service.SkillToolService;
import com.htam.agent.capability.tool.service.AgentToolService;
import com.htam.agent.capability.tool.service.ToolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 工具Service实现
 *
 * @author huxuehao
 */
@Service
@RequiredArgsConstructor
public class ToolServiceImpl implements ToolService {
    private final AgentDefinitionRepository agentDefinitionRepository;
    private final ToolConfigRepository toolConfigRepository;
    private final SkillToolService skillToolService;
    private final AgentToolService agentToolService;
    private final MessagePublisher messagePublisher;

    @Override
    public IPage<ToolConfig> page(PageParams pageParams, ToolDTO query) {
        ToolDTO toolQuery = query == null ? new ToolDTO() : query;
        RepoPage<ToolConfig> repoPage = toolConfigRepository.page(
                pageParams,
                toolQuery.getName(),
                toolQuery.getToolId(),
                toolQuery.getToolType(),
                toolQuery.getCategory(),
                toolQuery.getEnabled());
        IPage<ToolConfig> page = new Page<>(repoPage.current(), repoPage.size(), repoPage.total());
        page.setRecords(repoPage.records());
        return page;
    }

    @Override
    public ToolConfig getById(Long id) {
        return toolConfigRepository.getById(id);
    }

    @Override
    public ToolConfig getByToolId(String toolId) {
        return toolConfigRepository.getByToolId(toolId);
    }

    @Override
    public List<ToolConfig> listByIds(List<Long> ids) {
        return toolConfigRepository.listByIds(ids);
    }

    @Override
    public List<ToolConfig> listEnabledBriefByIds(List<Long> ids) {
        return toolConfigRepository.listEnabledBriefByIds(ids);
    }

    @Override
    public boolean save(ToolConfig entity) {
        return toolConfigRepository.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteTools(List<Long> ids) {
        // 删除前先获取关联的智能体ID，以便后续触发重新注册
        List<Long> agentIds = agentToolService.getAgentIds(ids);
        listByIds(ids).forEach(toolConfig -> {
            if (toolConfig.getToolType() != ToolType.BUILTIN) {
                toolConfigRepository.deleteById(toolConfig.getId());
            }
        });

        agentToolService.deleteByToolIds(ids);
        skillToolService.deleteByToolIds(ids);
        publishAgentReregister(agentIds);

        return true;
    }

    @Override
    public void SyncConfigToDatabase(List<ToolInfoWrapper> toolInfos) {
        toolConfigRepository.deleteBuiltinNotInClassPaths(toolInfos.stream()
                .map(ToolInfoWrapper::getClassPath)
                .toList());
        toolInfos.forEach(toolInfo -> {
            List<ToolConfig> list = toolConfigRepository.listBuiltinByClassPath(toolInfo.getClassPath());
            if (list.isEmpty()) {
                ToolConfig toolConfig = new ToolConfig();
                toolConfig.setName(toolInfo.getName());
                toolConfig.setToolId(toolInfo.getName());
                toolConfig.setToolType(ToolType.BUILTIN);
                toolConfig.setCategory("内置");
                toolConfig.setDescription(toolInfo.getDescription());
                toolConfig.setClassPath(toolInfo.getClassPath());
                toolConfig.setInputSchema(JsonUtils.toJsonNode(toolInfo.getParams()));
                toolConfig.setNeedConfirm(false);
                toolConfig.setEnabled(true);
                toolConfigRepository.save(toolConfig);
            } else {
                for (int i = 0; i < list.size(); i++) {
                    if (i == 0) {
                        list.get(i).setToolId(toolInfo.getName());
                        list.get(i).setToolType(ToolType.BUILTIN);
                        list.get(i).setClassPath(toolInfo.getClassPath());
                        list.get(i).setInputSchema(JsonUtils.toJsonNode(toolInfo.getParams()));
                        if (list.get(i).getNeedConfirm() == null) {
                            list.get(i).setNeedConfirm(false);
                        }
                        toolConfigRepository.updateById(list.get(i));
                    } else {
                        toolConfigRepository.deleteById(list.get(i).getId());
                    }
                }
            }
        });
    }

    @Override
    public List<Object> usedWithAgent(List<Long> ids) {
        List<Object> names = new ArrayList<>();
        agentDefinitionRepository.listByIds(agentToolService.getAgentIds(ids)).forEach(agentDefinition -> {
            names.add(agentDefinition.getName());
        });

        return names;
    }

    @Override
    public List<String> listCategories() {
        return toolConfigRepository.listCategories();
    }

    @Override
    public Boolean doUpdate(ToolConfig toolConfig) {
        boolean result;
        if (toolConfig.getToolType() != ToolType.BUILTIN) {
            result = toolConfigRepository.updateById(toolConfig);
        } else {
            result = toolConfigRepository.updateBuiltinEditableFields(toolConfig);
        }
        publishAgentReregister(agentToolService.getAgentIds(List.of(toolConfig.getId())));
        return result;
    }

    private void publishAgentReregister(List<Long> agentIds) {
        agentIds.forEach(agentId ->
                messagePublisher.publishAfterCommit(RedisChannelTopic.AGENT_REREGISTER_CHANNEL, String.valueOf(agentId)));
    }

}
