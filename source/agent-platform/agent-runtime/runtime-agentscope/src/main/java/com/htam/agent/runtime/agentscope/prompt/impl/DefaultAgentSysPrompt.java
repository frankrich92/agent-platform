package com.htam.agent.runtime.agentscope.prompt.impl;

import com.htam.agent.common.entity.AgentDefinition;
import com.htam.agent.common.entity.SystemPromptTemplate;
import com.htam.agent.repo.capability.SystemPromptTemplateRepository;
import com.htam.agent.runtime.agentscope.prompt.AgentSysPrompt;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 描述：默认系统提示词实现
 *
 * @author huxuehao
 **/
@Service
@RequiredArgsConstructor
public class DefaultAgentSysPrompt implements AgentSysPrompt {
    private final SystemPromptTemplateRepository systemPromptTemplateRepository;

    @Override
    public String getPrompt(AgentDefinition agentDefinition) {
        if (agentDefinition.getFollowTemplate() != null && agentDefinition.getFollowTemplate()) {
            SystemPromptTemplate promptTemplate = systemPromptTemplateRepository.getById(agentDefinition.getSystemPromptTemplateId());
            if (promptTemplate == null) {
                throw new RuntimeException("System prompt template not found");
            }

            if (promptTemplate.getEnabled()) {
                return promptTemplate.getContent();
            }

            throw new RuntimeException("System prompt template is disabled");
        } else {
            return agentDefinition.getSystemPrompt();
        }
    }

    @Override
    public int order() {
        return 0;
    }
}
