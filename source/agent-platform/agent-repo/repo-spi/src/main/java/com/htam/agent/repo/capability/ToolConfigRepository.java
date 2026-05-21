package com.htam.agent.repo.capability;

import com.htam.agent.common.entity.ToolConfig;
import com.htam.agent.common.enums.ToolType;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.repo.support.RepoPage;

import java.util.List;

public interface ToolConfigRepository {
    RepoPage<ToolConfig> page(PageParams pageParams,
                              String name,
                              String toolId,
                              ToolType toolType,
                              String category,
                              Boolean enabled);

    ToolConfig getById(Long id);

    ToolConfig getByToolId(String toolId);

    List<ToolConfig> listByIds(List<Long> ids);

    List<ToolConfig> listEnabledBriefByIds(List<Long> ids);

    List<ToolConfig> listBuiltinByClassPath(String classPath);

    boolean save(ToolConfig entity);

    boolean updateById(ToolConfig entity);

    boolean updateBuiltinEditableFields(ToolConfig entity);

    boolean deleteById(Long id);

    boolean deleteBuiltinNotInClassPaths(List<String> classPaths);

    List<String> listCategories();
}
