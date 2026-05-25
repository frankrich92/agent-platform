package com.htam.agent.runtime.agentscope.studio;

import com.htam.agent.common.entity.AgentDefinition;
import com.htam.agent.common.entity.AgentStudio;
import com.htam.agent.common.entity.StudioConfig;
import com.htam.agent.repo.agent.AgentStudioRepository;
import com.htam.agent.repo.agent.StudioConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 描述：StudioService
 *
 * @author huxuehao
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class StudioService {
    private final AgentStudioRepository agentStudioRepository;
    private final StudioConfigRepository studioConfigRepository;

    public boolean init(AgentDefinition definition) {
        AgentStudio agentStudio = agentStudioRepository.getFirstByAgentId(definition.getId());
        if (agentStudio != null) {
            StudioConfig studioConfig = studioConfigRepository.getById(agentStudio.getStudioId());
            if (studioConfig != null) {
                try {
                    StudioManagerUtils.initOnce(studioConfig.getUrl(), studioConfig.getProject(), definition.getAgentCode());
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    return false;
                }
                return true;
            }
        }

        return false;
    }
}
