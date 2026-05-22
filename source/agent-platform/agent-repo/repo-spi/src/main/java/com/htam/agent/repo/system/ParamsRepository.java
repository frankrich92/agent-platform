package com.htam.agent.repo.system;

import com.htam.agent.common.entity.Params;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.repo.support.RepoPage;

import java.util.List;

public interface ParamsRepository {
    RepoPage<Params> page(PageParams pageParams, String paramName, String paramKey, String paramValue);

    Params getById(Long id);

    Params getByKey(String key);

    boolean save(Params params);

    boolean updateValue(Long id, String value);

    boolean deleteByIds(List<Long> ids);
}
