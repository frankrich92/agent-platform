package com.htam.agent.common.knowledge;

import com.htam.agent.common.entity.KnowledgeBaseConfig;
import com.htam.agent.common.vo.RagDocumentChunkVO;
import java.util.List;

public interface KnowledgeRetrievalService {
    List<RagDocumentChunkVO> retrieve(String query,
                                      KnowledgeBaseConfig config,
                                      int limit,
                                      double scoreThreshold);
}
