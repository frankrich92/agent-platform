package com.htam.agent.runtime.agentscope.knowledge;

import com.htam.agent.common.entity.KnowledgeBaseConfig;
import com.htam.agent.common.enums.KbType;
import io.agentscope.core.rag.Knowledge;

/**
 * 描述：
 *
 * @author huxuehao
 **/
public interface IKnowledge {
    Knowledge build(KnowledgeBaseConfig knowledgeBaseConfig);

    KbType type();
}
