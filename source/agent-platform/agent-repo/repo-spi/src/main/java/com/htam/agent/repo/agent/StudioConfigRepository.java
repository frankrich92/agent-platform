package com.htam.agent.repo.agent;

import com.htam.agent.common.entity.StudioConfig;
import java.util.List;

public interface StudioConfigRepository {
    List<StudioConfig> list();

    StudioConfig getById(Long id);

    boolean save(StudioConfig entity);

    boolean updateById(StudioConfig entity);

    boolean deleteByIds(List<Long> ids);
}
