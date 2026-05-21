package com.htam.agent.agent.service;

import com.htam.agent.common.entity.AgentDefinition;
import com.htam.agent.common.entity.SkillPackage;
import com.htam.agent.common.entity.ToolConfig;
import com.htam.agent.common.vo.AgentDefinitionVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 智能体定义Service
 *
 * @author huxuehao
 */
public interface AgentDefinitionService extends IService<AgentDefinition> {
    AgentDefinitionVO agentDefinitionDetail(Long id);
    Boolean saveAgentDefinition(AgentDefinitionVO agentDefinition);
    Boolean updateAgentDefinition(AgentDefinitionVO agentDefinition);
    Boolean deleteAgentDefinition(List<Long> ids);
    List<Object> usedWithAgent(List<Long> ids);
    /**
     * 获取所有Tag
     *
     * @return Tag列表
     */
    List<String> listTags();
    List<String> allowFileType(Long id);
    List<ToolConfig> getEnabledToolsOfAgent(Long agentId);
    List<SkillPackage> getEnabledSkillsOfAgent(Long agentId);
}
