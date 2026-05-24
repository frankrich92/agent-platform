package com.htam.agent.repo.mybatis.knowledge;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.htam.agent.common.entity.RagDocumentChunk;
import com.htam.agent.repo.mybatis.knowledge.mapper.RagDocumentChunkMapper;
import com.htam.agent.repo.knowledge.RagDocumentChunkRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RagDocumentChunkMybatisRepository implements RagDocumentChunkRepository {
    private final RagDocumentChunkMapper ragDocumentChunkMapper;

    @Override
    public RagDocumentChunk getById(Long id) {
        return ragDocumentChunkMapper.selectById(id);
    }

    @Override
    public List<RagDocumentChunk> listByDocumentId(Long documentId) {
        return ragDocumentChunkMapper.selectList(Wrappers.<RagDocumentChunk>lambdaQuery()
                .eq(RagDocumentChunk::getDocumentId, documentId)
                .orderByAsc(RagDocumentChunk::getChunkIndex));
    }

    @Override
    public boolean save(RagDocumentChunk entity) {
        return ragDocumentChunkMapper.insert(entity) > 0;
    }

    @Override
    public boolean updateById(RagDocumentChunk entity) {
        return ragDocumentChunkMapper.updateById(entity) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return ragDocumentChunkMapper.deleteById(id) >= 0;
    }

    @Override
    public boolean deleteByDocumentId(Long documentId) {
        return ragDocumentChunkMapper.delete(Wrappers.<RagDocumentChunk>lambdaQuery()
                .eq(RagDocumentChunk::getDocumentId, documentId)) >= 0;
    }
}
