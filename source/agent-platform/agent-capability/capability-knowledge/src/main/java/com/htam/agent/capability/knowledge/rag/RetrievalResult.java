package com.htam.agent.capability.knowledge.rag;

/**
 * 向量检索结果
 *
 * @author huxuehao
 */
public record RetrievalResult(Long chunkId, Long documentId, double score) {
}
