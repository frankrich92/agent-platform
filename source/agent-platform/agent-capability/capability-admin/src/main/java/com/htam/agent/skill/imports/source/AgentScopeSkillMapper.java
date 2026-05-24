package com.htam.agent.skill.imports.source;

import com.htam.agent.skill.imports.ImportedSkill;
import io.agentscope.core.skill.AgentSkill;

final class AgentScopeSkillMapper {
    private AgentScopeSkillMapper() {
    }

    static ImportedSkill fromAgentScope(AgentSkill agentSkill) {
        return new ImportedSkill(
                agentSkill.getName(),
                agentSkill.getDescription(),
                agentSkill.getSkillContent(),
                agentSkill.getResources());
    }
}
