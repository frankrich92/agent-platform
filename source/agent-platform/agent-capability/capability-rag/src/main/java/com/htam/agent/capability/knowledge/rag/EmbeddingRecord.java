package com.htam.agent.capability.knowledge.rag;

/**
 * 嵌入向量记录
 *
 * @author huxuehao
 */
public record EmbeddingRecord(Long id, Long chunkId, Long documentId,
                              Long knowledgeBaseConfigId, float[] embedding) {
}
