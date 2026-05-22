package com.htam.agent.common.wrapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.htam.agent.common.enums.RagMode;
import io.agentscope.core.rag.Knowledge;
import lombok.*;

/**
 * 描述：Knowledge 包装类
 *
 * @author huxuehao
 **/
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeWrapper {
    private RagMode ragMode;
    private Knowledge knowledge;
    private JsonNode retrievalConfig;
}
