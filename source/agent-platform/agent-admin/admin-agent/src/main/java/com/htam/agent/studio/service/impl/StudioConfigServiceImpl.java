package com.htam.agent.studio.service.impl;

import com.htam.agent.common.entity.AgentDefinition;
import com.htam.agent.common.entity.StudioConfig;
import com.htam.agent.repo.agent.AgentDefinitionRepository;
import com.htam.agent.repo.agent.StudioConfigRepository;
import com.htam.agent.studio.service.AgentStudioService;
import com.htam.agent.studio.service.StudioConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述：StudioConfigServiceImpl
 *
 * @author huxuehao
 **/
@Service
@RequiredArgsConstructor
public class StudioConfigServiceImpl implements StudioConfigService {
    private final AgentDefinitionRepository agentDefinitionRepository;
    private final AgentStudioService agentStudioService;
    private final StudioConfigRepository studioConfigRepository;

    @Override
    public List<StudioConfig> list() {
        return studioConfigRepository.list();
    }

    @Override
    public StudioConfig getById(Long id) {
        return studioConfigRepository.getById(id);
    }

    @Override
    public Boolean save(StudioConfig entity) {
        return studioConfigRepository.save(entity);
    }

    @Override
    public Boolean updateById(StudioConfig entity) {
        return studioConfigRepository.updateById(entity);
    }

    @Override
    public Boolean removeByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }
        return studioConfigRepository.deleteByIds(ids);
    }

    @Override
    public List<Object> usedWithAgent(List<Long> ids) {
        List<Object> names = new ArrayList<>();
        agentDefinitionRepository.listByIds(agentStudioService.getAgentIds(ids)).forEach(agentDefinition -> {
            names.add(agentDefinition.getName());
        });

        return names;
    }
}
