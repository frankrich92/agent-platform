package com.htam.agent.capability.knowledge.agentscope.impl;

import com.htam.agent.common.entity.KnowledgeBaseConfig;
import com.htam.agent.common.enums.KbType;
import com.htam.agent.capability.knowledge.agentscope.IKnowledge;
import com.htam.agent.capability.knowledge.rag.knowledge.LocalKnowledge;
import com.htam.agent.knowledge.service.KnowledgeBaseConfigService;
import com.htam.agent.capability.knowledge.rag.service.LocalRagService;
import io.agentscope.core.rag.Knowledge;
import org.springframework.stereotype.Component;

/**
 * 本地RAG知识库实现，集成到KnowledgeFactory
 *
 * @author huxuehao
 */
@Component
public class LocalIKnowledge implements IKnowledge {

    private final LocalRagService localRagService;
    private final KnowledgeBaseConfigService knowledgeBaseConfigService;

    public LocalIKnowledge(LocalRagService localRagService,
                           KnowledgeBaseConfigService knowledgeBaseConfigService) {
        this.localRagService = localRagService;
        this.knowledgeBaseConfigService = knowledgeBaseConfigService;
    }

    @Override
    public Knowledge build(KnowledgeBaseConfig knowledgeBaseConfig) {
        return new LocalKnowledge(
                knowledgeBaseConfig.getId(),
                localRagService,
                knowledgeBaseConfigService
        );
    }

    @Override
    public KbType type() {
        return KbType.LOCAL;
    }
}
