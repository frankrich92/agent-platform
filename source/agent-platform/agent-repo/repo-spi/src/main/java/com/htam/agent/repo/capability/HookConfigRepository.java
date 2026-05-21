package com.htam.agent.repo.capability;

import com.htam.agent.common.entity.HookConfig;
import com.htam.agent.common.enums.HookType;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.repo.support.RepoPage;

import java.util.List;

public interface HookConfigRepository {
    RepoPage<HookConfig> page(PageParams pageParams, String name, HookType hookType, Boolean enabled);

    HookConfig getById(Long id);

    List<HookConfig> listByIds(List<Long> ids);

    List<HookConfig> listByClassPath(String classPath);

    boolean save(HookConfig entity);

    boolean updateById(HookConfig entity);

    boolean deleteById(Long id);

    boolean deleteByIds(List<Long> ids);

    boolean deleteBuiltinNotInClassPaths(List<String> classPaths);
}
