package com.htam.agent.capability.skill.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htam.agent.run.event.cluster.core.MessagePublisher;
import com.htam.agent.common.consts.RedisChannelTopic;
import com.htam.agent.common.dto.SkillPackageDTO;
import com.htam.agent.common.entity.SkillPackage;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.common.vo.SkillPackageVO;
import com.htam.agent.repo.agent.AgentDefinitionRepository;
import com.htam.agent.repo.capability.SkillPackageRepository;
import com.htam.agent.repo.support.RepoPage;
import com.htam.agent.capability.skill.service.AgentSkillPackageService;
import com.htam.agent.capability.skill.service.SkillPackageService;
import com.htam.agent.capability.skill.service.SkillToolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 技能包Service实现
 *
 * @author huxuehao
 */
@Service
@RequiredArgsConstructor
public class SkillPackageServiceImpl implements SkillPackageService {
    private final AgentDefinitionRepository agentDefinitionRepository;
    private final SkillPackageRepository skillPackageRepository;
    private final AgentSkillPackageService agentSkillPackageService;
    private final SkillToolService skillToolService;
    private final MessagePublisher messagePublisher;

    @Override
    public IPage<SkillPackage> page(PageParams pageParams, SkillPackageDTO query) {
        SkillPackageDTO skillQuery = query == null ? new SkillPackageDTO() : query;
        RepoPage<SkillPackage> repoPage = skillPackageRepository.page(
                pageParams,
                skillQuery.getName(),
                skillQuery.getCategory(),
                skillQuery.getEnabled());
        IPage<SkillPackage> page = new Page<>(repoPage.current(), repoPage.size(), repoPage.total());
        page.setRecords(repoPage.records());
        return page;
    }

    @Override
    public SkillPackage getById(Long id) {
        return skillPackageRepository.getById(id);
    }

    @Override
    public SkillPackage getByName(String name) {
        return skillPackageRepository.getByName(name);
    }

    @Override
    public List<SkillPackage> listByIds(List<Long> ids) {
        return skillPackageRepository.listByIds(ids);
    }

    @Override
    public List<SkillPackage> listEnabledBriefByIds(List<Long> ids) {
        return skillPackageRepository.listEnabledBriefByIds(ids);
    }

    @Override
    public List<SkillPackage> listWithScripts() {
        return skillPackageRepository.listWithScripts();
    }

    @Override
    public boolean save(SkillPackage entity) {
        return skillPackageRepository.save(entity);
    }

    @Override
    public boolean updateById(SkillPackage entity) {
        return skillPackageRepository.updateById(entity);
    }

    @Override
    public List<Object> usedWithAgent(List<Long> ids) {
        List<Object> names = new ArrayList<>();
        agentDefinitionRepository.listByIds(agentSkillPackageService.getAgentIds(ids)).forEach(agentDefinition -> {
            names.add(agentDefinition.getName());
        });

        return names;
    }

    @Override
    public List<String> listCategories() {
        return skillPackageRepository.listCategories();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByIds(List<Long> ids) {
        // 删除前先获取关联的智能体ID，以便后续触发重新注册
        List<Long> agentIds = agentSkillPackageService.getAgentIds(ids);
        skillPackageRepository.deleteByIds(ids);
        // 删除技能包与智能体的关联
        agentSkillPackageService.deleteBySkillPackageIds(ids);
        // 删除技能包与工具的关联
        skillToolService.deleteSkillTool(ids);
        publishAgentReregister(agentIds);
        return true;
    }

    @Override
    public boolean doUpdate(SkillPackage entity) {
        boolean result = skillPackageRepository.updateById(entity);
        publishAgentReregister(agentSkillPackageService.getAgentIds(List.of(entity.getId())));
        return result;
    }

    @Override
    public SkillPackageVO getDetail(Long id) {
        SkillPackage entity = skillPackageRepository.getById(id);
        if (entity == null) {
            return null;
        }
        SkillPackageVO vo = new SkillPackageVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setDescription(entity.getDescription());
        vo.setSkillContent(entity.getSkillContent());
        vo.setCategory(entity.getCategory());
        vo.setReferences(entity.getReferences());
        vo.setExamples(entity.getExamples());
        vo.setScripts(entity.getScripts());
        vo.setEnabled(entity.getEnabled());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        vo.setCreatedBy(entity.getCreatedBy());
        vo.setUpdatedBy(entity.getUpdatedBy());
        // 查询关联的工具ID列表
        vo.setTools(skillToolService.getToolIds(id));
        return vo;
    }

    private void publishAgentReregister(List<Long> agentIds) {
        agentIds.forEach(agentId ->
                messagePublisher.publish(RedisChannelTopic.AGENT_REREGISTER_CHANNEL, String.valueOf(agentId)));
    }

}
