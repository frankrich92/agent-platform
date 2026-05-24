package com.htam.agent.profile.agent.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.htam.agent.common.dto.AgentDefinitionDTO;
import com.htam.agent.common.entity.AgentDefinition;
import com.htam.agent.common.entity.SkillPackage;
import com.htam.agent.common.entity.ToolConfig;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.common.vo.AgentDefinitionVO;

import java.util.List;

/**
 * 智能体定义Service
 *
 * @author huxuehao
 */
public interface AgentDefinitionService {
    IPage<AgentDefinitionVO> pageAgentDefinitions(PageParams pageParams, AgentDefinitionDTO query);

    List<AgentDefinition> list();

    AgentDefinition getById(Long id);

    AgentDefinition getByAgentCode(String agentCode);

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
