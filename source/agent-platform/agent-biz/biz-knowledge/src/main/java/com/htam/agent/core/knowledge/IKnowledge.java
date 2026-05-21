package com.htam.agent.core.knowledge;

import com.htam.agent.common.entity.KnowledgeBaseConfig;
import com.htam.agent.common.enums.KbType;
import io.agentscope.core.rag.Knowledge;
import org.springframework.beans.factory.SmartInitializingSingleton;

/**
 * 描述：
 *
 * @author huxuehao
 **/
public interface IKnowledge extends SmartInitializingSingleton {
    Knowledge build(KnowledgeBaseConfig knowledgeBaseConfig);

    KbType type();

    @Override
    default void afterSingletonsInstantiated() {
        KnowledgeFactory.register(this);
    }
}
