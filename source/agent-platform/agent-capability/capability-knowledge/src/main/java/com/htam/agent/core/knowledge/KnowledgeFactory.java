package com.htam.agent.core.knowledge;

import com.htam.agent.common.entity.AgentDefinition;
import com.htam.agent.common.entity.KnowledgeBaseConfig;
import com.htam.agent.common.enums.KbType;
import com.htam.agent.capability.knowledge.KnowledgeWrapper;
import com.htam.agent.knowledge.service.KnowledgeBaseConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 描述：知识库工程
 *
 * @author huxuehao
 **/
@Component
@RequiredArgsConstructor
public class KnowledgeFactory {
    private static final Map<KbType, IKnowledge> KNOWLEDGE_MAP = new ConcurrentHashMap<>();

    private final KnowledgeBaseConfigService knowledgeBaseConfigService;

    public KnowledgeWrapper getKnowledge(AgentDefinition definition) {

        KnowledgeBaseConfig knowledgeBaseConfig = knowledgeBaseConfigService.getByAgentId(definition.getId());
        if (knowledgeBaseConfig == null) {
            return null;
        }

        if (!knowledgeBaseConfig.getEnabled()) {
            return null;
        }

        IKnowledge iKnowledge = KNOWLEDGE_MAP.get(knowledgeBaseConfig.getKbType());
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

    public static void register(IKnowledge knowledge) {
        KNOWLEDGE_MAP.put(knowledge.type(), knowledge);
    }

    public static void unregister(KbType type) {
        KNOWLEDGE_MAP.remove(type);
    }

    public static void clear() {
        KNOWLEDGE_MAP.clear();
    }
}
