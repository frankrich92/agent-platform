package com.htam.agent.runtime.agentscope.knowledge.impl;

import com.htam.agent.common.entity.KnowledgeBaseConfig;
import com.htam.agent.common.enums.KbType;
import com.htam.agent.common.knowledge.KnowledgeRetrievalService;
import com.htam.agent.repo.knowledge.KnowledgeBaseConfigRepository;
import com.htam.agent.runtime.agentscope.knowledge.IKnowledge;
import com.htam.agent.runtime.agentscope.knowledge.rag.LocalKnowledge;
import io.agentscope.core.rag.Knowledge;
import org.springframework.stereotype.Component;

/**
 * 本地RAG知识库实现，集成到KnowledgeFactory
 *
 * @author huxuehao
 */
@Component
public class LocalIKnowledge implements IKnowledge {

    private final KnowledgeRetrievalService knowledgeRetrievalService;
    private final KnowledgeBaseConfigRepository knowledgeBaseConfigRepository;

    public LocalIKnowledge(KnowledgeRetrievalService knowledgeRetrievalService,
                           KnowledgeBaseConfigRepository knowledgeBaseConfigRepository) {
        this.knowledgeRetrievalService = knowledgeRetrievalService;
        this.knowledgeBaseConfigRepository = knowledgeBaseConfigRepository;
    }

    @Override
    public Knowledge build(KnowledgeBaseConfig knowledgeBaseConfig) {
        return new LocalKnowledge(
                knowledgeBaseConfig.getId(),
                knowledgeRetrievalService,
                knowledgeBaseConfigRepository
        );
    }

    @Override
    public KbType type() {
        return KbType.LOCAL;
    }
}
