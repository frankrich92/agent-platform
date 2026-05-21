package com.htam.agent.repo.agent;

import com.htam.agent.common.entity.CodeExecutionConfig;
import java.util.List;

public interface CodeExecutionConfigRepository {
    List<CodeExecutionConfig> list();

    CodeExecutionConfig getById(Long id);

    boolean save(CodeExecutionConfig entity);

    boolean updateById(CodeExecutionConfig entity);

    boolean deleteByIds(List<Long> ids);
}
