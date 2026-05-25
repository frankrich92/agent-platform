package com.htam.agent.repo.vector;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.htam.agent.repo.knowledge.PgVectorRepository;
import com.htam.agent.repo.knowledge.VectorSearchRecord;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PgVectorRepositoryAdapterTest {

    @Test
    void mapsVectorRepositoryContractToPgVectorRepository() {
        FakePgVectorRepository delegate = new FakePgVectorRepository();
        PgVectorRepositoryAdapter adapter = new PgVectorRepositoryAdapter(delegate);

        adapter.upsert(new VectorRecord(
                "1",
                "kb:10",
                new float[] {1.0f, 0.0f},
                Map.of("chunkId", 2L, "documentId", 3L)));
        List<VectorSearchResult> results = adapter.search("kb:10", new float[] {1.0f, 0.0f}, 5, 0.1, Map.of());

        assertEquals(2L, delegate.chunkId);
        assertEquals(10L, delegate.knowledgeBaseConfigId);
        assertEquals(1, results.size());
        assertEquals("2", results.getFirst().record().id());
    }

    private static final class FakePgVectorRepository implements PgVectorRepository {
        private Long chunkId;
        private Long knowledgeBaseConfigId;

        @Override
        public boolean isAvailable() {
            return true;
        }

        @Override
        public void storeEmbedding(Long id, Long chunkId, Long documentId, Long knowledgeBaseConfigId, float[] embedding) {
            this.chunkId = chunkId;
            this.knowledgeBaseConfigId = knowledgeBaseConfigId;
        }

        @Override
        public List<VectorSearchRecord> search(float[] queryEmbedding, Long knowledgeBaseConfigId, int limit) {
            return List.of(new VectorSearchRecord(2L, 3L, 0.9));
        }

        @Override
        public void deleteByDocumentId(Long documentId) {
        }

        @Override
        public void deleteByKnowledgeBaseConfigId(Long knowledgeBaseConfigId) {
        }

        @Override
        public void deleteByChunkId(Long chunkId) {
        }
    }
}
