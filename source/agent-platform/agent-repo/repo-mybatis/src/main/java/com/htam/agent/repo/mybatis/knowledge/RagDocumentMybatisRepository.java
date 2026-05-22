package com.htam.agent.repo.mybatis.knowledge;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.htam.agent.common.entity.RagDocument;
import com.htam.agent.core.rag.mapper.RagDocumentMapper;
import com.htam.agent.repo.knowledge.RagDocumentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RagDocumentMybatisRepository implements RagDocumentRepository {
    private final RagDocumentMapper ragDocumentMapper;

    @Override
    public RagDocument getById(Long id) {
        return ragDocumentMapper.selectById(id);
    }

    @Override
    public List<RagDocument> listByKnowledgeBaseConfigId(Long knowledgeBaseConfigId) {
        return ragDocumentMapper.selectList(Wrappers.<RagDocument>lambdaQuery()
                .eq(RagDocument::getKnowledgeBaseConfigId, knowledgeBaseConfigId)
                .orderByDesc(RagDocument::getCreatedAt));
    }

    @Override
    public boolean save(RagDocument entity) {
        return ragDocumentMapper.insert(entity) > 0;
    }

    @Override
    public boolean updateById(RagDocument entity) {
        return ragDocumentMapper.updateById(entity) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return ragDocumentMapper.deleteById(id) >= 0;
    }
}
