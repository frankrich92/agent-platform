package com.htam.agent.worker.code.service.impl;

import com.htam.agent.worker.code.service.AgentCodeExecutionService;
import com.htam.agent.worker.code.service.CodeExecutionConfigService;
import com.htam.agent.common.entity.CodeExecutionConfig;
import com.htam.agent.repo.agent.AgentDefinitionRepository;
import com.htam.agent.repo.agent.CodeExecutionConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述：CodeExecutionConfigServiceImpl
 *
 * @author huxuehao
 **/
@Service
@RequiredArgsConstructor
public class CodeExecutionConfigServiceImpl implements CodeExecutionConfigService {
    private final AgentDefinitionRepository agentDefinitionRepository;
    private final CodeExecutionConfigRepository codeExecutionConfigRepository;
    private final AgentCodeExecutionService agentCodeExecutionService;

    @Override
    public List<CodeExecutionConfig> list() {
        return codeExecutionConfigRepository.list();
    }

    @Override
    public CodeExecutionConfig getById(Long id) {
        return codeExecutionConfigRepository.getById(id);
    }

    @Override
    public boolean save(CodeExecutionConfig entity) {
        return codeExecutionConfigRepository.save(entity);
    }

    @Override
    public boolean updateById(CodeExecutionConfig entity) {
        return codeExecutionConfigRepository.updateById(entity);
    }

    @Override
    public boolean removeByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }
        return codeExecutionConfigRepository.deleteByIds(ids);
    }

    @Override
    public List<Object> usedWithAgent(List<Long> ids) {
        List<Object> names = new ArrayList<>();
        agentDefinitionRepository.listByIds(agentCodeExecutionService.getAgentIds(ids)).forEach(agentDefinition -> {
            names.add(agentDefinition.getName());
        });

        return names;
    }
}
