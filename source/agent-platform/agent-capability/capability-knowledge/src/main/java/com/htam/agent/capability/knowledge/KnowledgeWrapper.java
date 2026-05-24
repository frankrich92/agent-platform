package com.htam.agent.capability.knowledge;

import com.fasterxml.jackson.databind.JsonNode;
import com.htam.agent.common.enums.RagMode;
import io.agentscope.core.rag.Knowledge;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
