package com.htam.agent.studio.service;

import java.util.List;
import java.util.Map;

/**
 * 描述：AgentStudioService
 *
 * @author huxuehao
 **/
public interface AgentStudioService {
    List<Long> getAgentIds(List<Long> studioId);
    Long getStudioIdByAgentId(Long agentId);
    Map<Long, Long> getStudioIdsByAgentIds(List<Long> agentIds);
    Boolean insertAgentStudio(Long agentDefinitionId, List<Long> studioIds);
    Boolean deleteAgentStudio(List<Long> agentIds);
    Boolean saveAgentStudio(Long agentDefinitionId, List<Long> studioIds);
}
