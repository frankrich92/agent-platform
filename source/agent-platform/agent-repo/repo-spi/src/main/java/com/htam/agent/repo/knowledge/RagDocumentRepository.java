package com.htam.agent.repo.knowledge;

import com.htam.agent.common.entity.RagDocument;
import java.util.List;

public interface RagDocumentRepository {
    RagDocument getById(Long id);

    List<RagDocument> listByKnowledgeBaseConfigId(Long knowledgeBaseConfigId);

    boolean save(RagDocument entity);

    boolean updateById(RagDocument entity);

    boolean deleteById(Long id);
}
