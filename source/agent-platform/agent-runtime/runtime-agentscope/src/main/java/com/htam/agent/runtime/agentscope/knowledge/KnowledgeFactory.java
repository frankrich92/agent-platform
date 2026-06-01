package com.htam.agent.runtime.agentscope.knowledge;

import com.htam.agent.common.entity.AgentDefinition;
import com.htam.agent.common.entity.AgentKnowledgeBase;
import com.htam.agent.common.entity.KnowledgeBaseConfig;
import com.htam.agent.common.enums.KbType;
import com.htam.agent.repo.knowledge.AgentKnowledgeBaseRepository;
import com.htam.agent.repo.knowledge.KnowledgeBaseConfigRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * 描述：知识库工程
 *
 * @author huxuehao
 **/
@Component
public class KnowledgeFactory {
    private final AgentKnowledgeBaseRepository agentKnowledgeBaseRepository;
    private final KnowledgeBaseConfigRepository knowledgeBaseConfigRepository;
    private final Map<KbType, IKnowledge> knowledgeMap;

    public KnowledgeFactory(
            AgentKnowledgeBaseRepository agentKnowledgeBaseRepository,
            KnowledgeBaseConfigRepository knowledgeBaseConfigRepository,
            List<IKnowledge> knowledgeStrategies) {
        this.agentKnowledgeBaseRepository = agentKnowledgeBaseRepository;
        this.knowledgeBaseConfigRepository = knowledgeBaseConfigRepository;
        Map<KbType, IKnowledge> strategies = new LinkedHashMap<>();
        for (IKnowledge strategy : knowledgeStrategies) {
            strategies.put(strategy.type(), strategy);
        }
        this.knowledgeMap = Map.copyOf(strategies);
    }

    public KnowledgeWrapper getKnowledge(AgentDefinition definition) {

        KnowledgeBaseConfig knowledgeBaseConfig = getByAgentId(definition.getId());
        if (knowledgeBaseConfig == null) {
            return null;
        }

        if (!knowledgeBaseConfig.getEnabled()) {
            return null;
        }

        IKnowledge iKnowledge = knowledgeMap.get(knowledgeBaseConfig.getKbType());
        if (iKnowledge == null) {
            return null;
        }

        return KnowledgeWrapper
                .builder()
                .ragMode(knowledgeBaseConfig.getRagMode())
                .knowledge(iKnowledge.build(knowledgeBaseConfig))
                .retrievalConfig(knowledgeBaseConfig.getRetrievalConfig())
                .build();
    }

    private KnowledgeBaseConfig getByAgentId(Long agentId) {
        List<Long> knowledgeIds = agentKnowledgeBaseRepository.listByAgentId(agentId)
                .stream()
                .map(AgentKnowledgeBase::getKnowledgeBaseConfigId)
                .toList();
        if (knowledgeIds.isEmpty()) {
            return null;
        }

        List<KnowledgeBaseConfig> knowledgeBaseConfigs = knowledgeBaseConfigRepository.listByIds(knowledgeIds);
        if (knowledgeBaseConfigs == null || knowledgeBaseConfigs.isEmpty()) {
            return null;
        }

        return knowledgeBaseConfigs.getFirst();
    }
}
