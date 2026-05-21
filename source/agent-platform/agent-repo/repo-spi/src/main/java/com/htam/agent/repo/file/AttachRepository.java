package com.htam.agent.repo.file;

import com.htam.agent.common.entity.Attach;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.repo.support.RepoPage;

import java.util.List;

public interface AttachRepository {
    RepoPage<Attach> page(PageParams pageParams,
                          Long fileId,
                          String link,
                          String domain,
                          String name,
                          String originalName,
                          String extension);

    Attach getById(Long id);

    List<Attach> listByIds(List<Long> ids);

    boolean save(Attach attach);

    boolean deleteById(Long id);
}
