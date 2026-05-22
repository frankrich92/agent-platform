package com.htam.agent.repo.agent;

import com.htam.agent.common.entity.AgentDefinition;
import com.htam.agent.common.enums.AgentType;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.repo.support.RepoPage;
import java.util.List;

public interface AgentDefinitionRepository {
    RepoPage<AgentDefinition> page(PageParams pageParams,
                                   String name,
                                   AgentType agentType,
                                   String agentCode,
                                   String tag,
                                   Boolean enabled);

    List<AgentDefinition> list();

    AgentDefinition getById(Long id);

    AgentDefinition getByAgentCode(String agentCode);

    boolean save(AgentDefinition entity);

    boolean updateById(AgentDefinition entity);

    List<AgentDefinition> listByIds(List<Long> ids);

    List<AgentDefinition> listByModelConfigIds(List<Long> modelConfigIds);

    List<AgentDefinition> listBySensitiveWordConfigIds(List<Long> sensitiveWordConfigIds);

    List<AgentDefinition> listBySystemPromptTemplateIds(List<Long> systemPromptTemplateIds);

    boolean deleteByIds(List<Long> ids);

    List<String> listTags();
}
