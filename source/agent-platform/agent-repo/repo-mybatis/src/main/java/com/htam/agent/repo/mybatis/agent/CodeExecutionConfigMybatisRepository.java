package com.htam.agent.repo.mybatis.agent;

import com.htam.agent.repo.mybatis.agent.mapper.CodeExecutionConfigMapper;
import com.htam.agent.common.entity.CodeExecutionConfig;
import com.htam.agent.repo.agent.CodeExecutionConfigRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CodeExecutionConfigMybatisRepository implements CodeExecutionConfigRepository {
    private final CodeExecutionConfigMapper codeExecutionConfigMapper;

    @Override
    public List<CodeExecutionConfig> list() {
        return codeExecutionConfigMapper.selectList(null);
    }

    @Override
    public CodeExecutionConfig getById(Long id) {
        return codeExecutionConfigMapper.selectById(id);
    }

    @Override
    public boolean save(CodeExecutionConfig entity) {
        return codeExecutionConfigMapper.insert(entity) > 0;
    }

    @Override
    public boolean updateById(CodeExecutionConfig entity) {
        return codeExecutionConfigMapper.updateById(entity) > 0;
    }

    @Override
    public boolean deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }
        return codeExecutionConfigMapper.deleteBatchIds(ids) >= 0;
    }
}
