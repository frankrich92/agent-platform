package com.htam.agent.repo.knowledge;

import com.htam.agent.common.entity.RagDocumentChunk;
import java.util.List;

public interface RagDocumentChunkRepository {
    RagDocumentChunk getById(Long id);

    List<RagDocumentChunk> listByDocumentId(Long documentId);

    boolean save(RagDocumentChunk entity);

    boolean updateById(RagDocumentChunk entity);

    boolean deleteById(Long id);

    boolean deleteByDocumentId(Long documentId);
}
