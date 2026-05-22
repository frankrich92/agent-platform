package com.htam.agent.repo.file;

import com.htam.agent.common.entity.AttachLog;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.repo.support.RepoPage;

public interface AttachLogRepository {
    RepoPage<AttachLog> page(PageParams pageParams,
                             Long fileId,
                             String originalName,
                             String extension,
                             String optType);

    boolean save(AttachLog attachLog);
}
