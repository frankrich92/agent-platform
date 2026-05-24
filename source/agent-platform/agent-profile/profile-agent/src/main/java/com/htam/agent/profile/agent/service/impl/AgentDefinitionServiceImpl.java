package com.htam.agent.profile.agent.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.htam.agent.profile.a2a.service.AgentA2aService;
import com.htam.agent.profile.agent.service.AgentDefinitionService;
import com.htam.agent.profile.agent.service.AgentSubAgentService;
import com.htam.agent.cluster.core.MessagePublisher;
import com.htam.agent.common.consts.RedisChannelTopic;
import com.htam.agent.common.dto.AgentDefinitionDTO;
import com.htam.agent.common.entity.*;
import com.htam.agent.common.enums.AgentType;
import com.htam.agent.common.enums.ModelType;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.common.util.BeanUtils;
import com.htam.agent.common.util.JsonUtils;
import com.htam.agent.common.vo.AgentDefinitionVO;
import com.htam.agent.common.vo.SkillPackageVO;
import com.htam.agent.common.vo.ToolVO;
import com.htam.agent.capability.tool.hook.service.AgentHookService;
import com.htam.agent.capability.knowledge.service.AgentKnowledgeBaseService;
import com.htam.agent.capability.mcp.service.AgentMcpServerService;
import com.htam.agent.capability.provider.service.ModelConfigService;
import com.htam.agent.params.core.ParamsAdapter;
import com.htam.agent.capability.skill.service.AgentSkillPackageService;
import com.htam.agent.capability.skill.service.SkillPackageService;
import com.htam.agent.studio.service.AgentStudioService;
import com.htam.agent.capability.tool.service.AgentToolService;
import com.htam.agent.profile.agent.service.AgentCodeExecutionService;
import com.htam.agent.capability.tool.service.ToolService;
import com.htam.agent.repo.agent.AgentDefinitionRepository;
import com.htam.agent.repo.agent.JobInfoRepository;
import com.htam.agent.repo.support.RepoPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 智能体定义Service实现
 *
 * @author huxuehao
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AgentDefinitionServiceImpl implements AgentDefinitionService {
    private final AgentDefinitionRepository agentDefinitionRepository;
    private final AgentHookService agentHookService;
    private final AgentToolService agentToolService;
    private final ToolService toolService;
    private final AgentMcpServerService agentMcpServerService;
    private final AgentSkillPackageService agentSkillPackageService;
    private final SkillPackageService skillPackageService;
    private final AgentSubAgentService agentSubAgentService;
    private final AgentKnowledgeBaseService agentKnowledgeBaseService;
    private final ModelConfigService modelConfigService;
    private final ParamsAdapter paramsAdapter;
    private final AgentA2aService agentA2aService;
    private final AgentStudioService agentStudioService;
    private final JobInfoRepository jobInfoRepository;
    private final AgentCodeExecutionService agentCodeExecutionService;
    private final MessagePublisher messagePublisher;

    @Override
    public IPage<AgentDefinitionVO> pageAgentDefinitions(PageParams pageParams, AgentDefinitionDTO query) {
        RepoPage<AgentDefinition> repoPage = agentDefinitionRepository.page(
                pageParams,
                query.getName(),
                query.getAgentType(),
                query.getAgentCode(),
                query.getTag(),
                query.getEnabled());
        IPage<AgentDefinition> page = new Page<>(repoPage.current(), repoPage.size(), repoPage.total());
        page.setRecords(repoPage.records());
        IPage<AgentDefinitionVO> pageVo = BeanUtils.copyPage(page, AgentDefinitionVO.class);
        fillListDerivedFields(pageVo.getRecords());
        return pageVo;
    }

    @Override
    public List<AgentDefinition> list() {
        return agentDefinitionRepository.list();
    }

    @Override
    public AgentDefinition getById(Long id) {
        return agentDefinitionRepository.getById(id);
    }

    @Override
    public AgentDefinition getByAgentCode(String agentCode) {
        return agentDefinitionRepository.getByAgentCode(agentCode);
    }

    @Override
    public AgentDefinitionVO agentDefinitionDetail(Long id) {
        AgentDefinition entity = getById(id);
        if (entity == null) {
            throw new RuntimeException("AgentDefinition not found for id: " + id);
        }

        AgentDefinitionVO vo = BeanUtils.copy(entity, AgentDefinitionVO.class);

        vo.setHook(agentHookService.getHookIds(id));
        vo.setUsed(usedWithAgent(List.of(id)));
        List<JobInfo> agentJobs = jobInfoRepository.listAgentJobsByBizId(id);
        if (agentJobs.size() == 1) {
            vo.setJobInfo(agentJobs.getFirst());
        }
        Long studioConfigId = agentStudioService.getStudioIdByAgentId(id);
        if (studioConfigId != null) {
            vo.setStudioConfigId(studioConfigId);
        }
        Long codeExecutionId = agentCodeExecutionService.getCodeExecutionIdByAgentId(id);
        if (codeExecutionId != null) {
            vo.setCodeExecutionConfigId(codeExecutionId);
        }

        if(entity.getAgentType() == AgentType.CUSTOM) {
            vo.setTool(agentToolService.getToolIds(id));
            vo.setMcp(agentMcpServerService.getMcpIds(id));
            vo.setMcpBindings(agentMcpServerService.getBindings(id));
            vo.setSkill(agentSkillPackageService.getSkillPackageIds(id));
            vo.setSubAgent(agentSubAgentService.getSubAgentIds(id));
            vo.setKnowledgeBase(agentKnowledgeBaseService.getKnowledgeIds(id));
        } else {
            vo.setAgentA2A(agentA2aService.getA2aConfigByAgentId(id));
        }

        return vo;
    }

    private void fillListDerivedFields(List<AgentDefinitionVO> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        List<JobInfo> agentJobs = jobInfoRepository.listAgentJobs();
        Map<String, JobInfo> jobMap = agentJobs.stream().collect(Collectors.toMap(
                JobInfo::getBizId,
                item -> item,
                (existing, replacement) -> existing));
        Map<Long, Long> studioMap = agentStudioService.getStudioIdsByAgentIds(records.stream()
                .map(AgentDefinitionVO::getId)
                .toList());
        records.forEach(agentVo -> {
            JobInfo jobInfo = jobMap.get(String.valueOf(agentVo.getId()));
            if (jobInfo != null) {
                agentVo.setJobInfo(jobInfo);
            }
            Long studioId = studioMap.get(agentVo.getId());
            if (studioId != null) {
                agentVo.setStudioConfigId(studioId);
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveAgentDefinition(AgentDefinitionVO vo) {
        AgentDefinition agentDefinition = BeanUtils.copy(vo, AgentDefinition.class);
        agentDefinitionRepository.save(agentDefinition);
        vo.setId(agentDefinition.getId());

        saveSubItems(vo);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateAgentDefinition(AgentDefinitionVO vo) {
        agentDefinitionRepository.updateById(BeanUtils.copy(vo, AgentDefinition.class));

        if (vo.getAgentCode() == null) {
            List<JobInfo> agent = jobInfoRepository.listAgentJobsByBizId(vo.getId());
            if (!agent.isEmpty() && agent.getFirst().isEnabled()) {
                throw new RuntimeException("请先禁用定时任务");
            }
            if (vo.getEnabled()) {
                messagePublisher.publish(RedisChannelTopic.AGENT_REREGISTER_CHANNEL, String.valueOf(vo.getId()));
            } else {
                AgentDefinition agentDefinition = getById(vo.getId());
                messagePublisher.publish(RedisChannelTopic.AGENT_UNREGISTER_CHANNEL, agentDefinition.getAgentCode());
            }

            return true;
        }

        saveSubItems(vo);

        messagePublisher.publish(RedisChannelTopic.AGENT_REREGISTER_CHANNEL, String.valueOf(vo.getId()));
        return true;
    }

    private void saveSubItems(AgentDefinitionVO vo) {
        agentHookService.saveAgentHook(vo.getId(), vo.getHook());
        if (vo.getAgentType() == AgentType.CUSTOM) {
            agentSubAgentService.saveSubAgent(vo.getId(), vo.getSubAgent());
            agentToolService.saveAgentTool(vo.getId(), vo.getTool());
            agentMcpServerService.saveAgentMcpServer(vo.getId(), vo.getMcp(), vo.getMcpBindings());
            agentSkillPackageService.saveAgentSkillPackage(vo.getId(), vo.getSkill());
            agentKnowledgeBaseService.saveAgentKnowledge(vo.getId(), vo.getKnowledgeBase());
            if (vo.getStudioConfigId() != null) {
                agentStudioService.saveAgentStudio(vo.getId(), List.of(vo.getStudioConfigId()));
            } else {
                agentStudioService.deleteAgentStudio(List.of(vo.getId()));
            }
            if (vo.getCodeExecutionConfigId() != null) {
                agentCodeExecutionService.saveAgentCodeExecution(vo.getId(), List.of(vo.getCodeExecutionConfigId()));
            } else {
                agentCodeExecutionService.deleteAgentCodeExecution(List.of(vo.getId()));
            }
        } else {
            AgentA2A agentA2A = vo.getAgentA2A();
            agentA2A.setAgentDefinitionId(vo.getId());
            agentA2aService.saveA2aConfig(agentA2A);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteAgentDefinition(List<Long> ids) {
        List<JobInfo> agent = jobInfoRepository.listAgentJobsByBizIds(ids);
        if (!agent.isEmpty()) {
            throw new RuntimeException("请先解绑定时任务");
        }

        List<AgentDefinition> agents = agentDefinitionRepository.listByIds(ids);

        agentDefinitionRepository.deleteByIds(ids);
        agentA2aService.deleteA2aConfig(ids);
        agentSubAgentService.deleteSubAgent(ids);
        agentHookService.deleteAgentHook(ids);
        agentToolService.deleteAgentTool(ids);
        agentMcpServerService.deleteAgentMcpServer(ids);
        agentSkillPackageService.deleteAgentSkillPackage(ids);
        agentKnowledgeBaseService.deleteAgentKnowledge(ids);
        agentStudioService.deleteAgentStudio(ids);
        agentCodeExecutionService.deleteAgentCodeExecution(ids);

        for (AgentDefinition agent_ : agents) {
            messagePublisher.publish(RedisChannelTopic.AGENT_UNREGISTER_CHANNEL, agent_.getAgentCode());
        }

        return Boolean.TRUE;
    }

    @Override
    public List<Object> usedWithAgent(List<Long> ids) {
        List<Object> names = new ArrayList<>();
        ids.forEach(id -> {
            agentSubAgentService.getSubAgentIds(id).forEach(subAgentId -> {
                AgentDefinition agentDefinition = getById(subAgentId);
                if (agentDefinition != null) {
                    names.add(agentDefinition.getName());
                }
            });
        });

        return names;
    }

    @Override
    public List<String> listTags() {
        return agentDefinitionRepository.listTags();
    }

    @Override
    public List<String> allowFileType(Long id) {
        AgentDefinition agentDefinition = getById(id);
        if (agentDefinition == null || !agentDefinition.getEnabled()) {
            return List.of();
        }

        ModelConfig modelConfig = modelConfigService.getById(agentDefinition.getModelConfigId());
        if (modelConfig == null) {
            return List.of();
        }
        JsonNode modelTypeJ = modelConfig.getModelType();
        if (modelTypeJ == null) {
            return List.of();
        }

        List<String> modelType = parseModelType(modelTypeJ);
        List<String> allowImageFileType = new ArrayList<>();
        if (modelType.contains(ModelType.IMAGE.name())) {
            allowImageFileType.add(paramsAdapter.getValue("ALLOW_IMAGE_FILE_TYPE"));
        }
        if (modelType.contains(ModelType.AUDIO.name())) {
            allowImageFileType.add(paramsAdapter.getValue("ALLOW_AUDIO_FILE_TYPE"));
        }
        if (modelType.contains(ModelType.VIDEO.name())) {
            allowImageFileType.add(paramsAdapter.getValue("ALLOW_VIDEO_FILE_TYPE"));
        }

        if (allowImageFileType.isEmpty()) {
            return List.of();
        }

        String join = String.join(",", allowImageFileType);
        return List.of(join.split(","));
    }

    @Override
    public List<ToolConfig> getEnabledToolsOfAgent(Long agentId) {
        List<Long> toolIds = agentToolService.getToolIds(agentId);
        if (!toolIds.isEmpty()) {
            return toolService.listEnabledBriefByIds(toolIds);
        }
        return List.of();
    }

    @Override
    public List<SkillPackage> getEnabledSkillsOfAgent(Long agentId) {
        List<Long> skillPackageIds = agentSkillPackageService.getSkillPackageIds(agentId);
        if (!skillPackageIds.isEmpty()) {
            return skillPackageService.listEnabledBriefByIds(skillPackageIds);
        }
        return Collections.emptyList();
    }

    private List<String> parseModelType(JsonNode modelTypeJ) {
        try {
            return (List<String>)JsonUtils.parse(modelTypeJ.toString(), List.class);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
