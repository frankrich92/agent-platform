package com.htam.agent.repo.vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class InMemoryVectorRepositoryTest {

    @Test
    void searchReturnsNearestVector() {
        InMemoryVectorRepository repository = new InMemoryVectorRepository();
        repository.upsert(new VectorRecord("a", "kb", new float[] {1, 0}, Map.of()));
        repository.upsert(new VectorRecord("b", "kb", new float[] {0, 1}, Map.of()));

        VectorSearchResult result = repository.search(new float[] {0.9f, 0.1f}, 1).getFirst();

        assertEquals("a", result.record().id());
    }

    @Test
    void searchSupportsCollectionMetadataScoreAndDelete() {
        InMemoryVectorRepository repository = new InMemoryVectorRepository();
        repository.upsert(new VectorRecord("a", "kb-a", new float[] {1, 0}, Map.of("tenant", "t1")));
        repository.upsert(new VectorRecord("b", "kb-b", new float[] {1, 0}, Map.of("tenant", "t1")));
        repository.upsert(new VectorRecord("c", "kb-a", new float[] {0, 1}, Map.of("tenant", "t2")));

        List<VectorSearchResult> results = repository.search(
                "kb-a",
                new float[] {1, 0},
                10,
                0.5,
                Map.of("tenant", "t1"));

        assertEquals(1, results.size());
        assertEquals("a", results.getFirst().record().id());
        assertTrue(repository.findById("a").isPresent());
        assertTrue(repository.delete("a"));
        assertTrue(repository.findById("a").isEmpty());
    }
}
