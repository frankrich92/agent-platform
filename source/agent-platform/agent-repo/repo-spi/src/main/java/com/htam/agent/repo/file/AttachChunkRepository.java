package com.htam.agent.repo.file;

import com.htam.agent.common.entity.AttachChunk;

import java.util.List;

public interface AttachChunkRepository {
    List<AttachChunk> listByFileKey(String fileKey);

    boolean save(AttachChunk chunk);

    boolean deleteByFileKey(String fileKey);
}
