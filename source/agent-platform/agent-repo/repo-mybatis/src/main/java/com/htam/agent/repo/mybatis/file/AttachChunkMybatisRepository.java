package com.htam.agent.repo.mybatis.file;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.htam.agent.common.entity.AttachChunk;
import com.htam.agent.repo.file.AttachChunkRepository;
import com.htam.agent.resource.mapper.AttachChunkMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AttachChunkMybatisRepository implements AttachChunkRepository {
    private final AttachChunkMapper attachChunkMapper;

    @Override
    public List<AttachChunk> listByFileKey(String fileKey) {
        return attachChunkMapper.selectList(Wrappers.<AttachChunk>lambdaQuery()
                .eq(AttachChunk::getFileKey, fileKey));
    }

    @Override
    public boolean save(AttachChunk chunk) {
        return attachChunkMapper.insert(chunk) > 0;
    }

    @Override
    public boolean deleteByFileKey(String fileKey) {
        return attachChunkMapper.delete(Wrappers.<AttachChunk>lambdaQuery()
                .eq(AttachChunk::getFileKey, fileKey)) >= 0;
    }
}
