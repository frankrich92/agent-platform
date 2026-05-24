package com.htam.agent.repo.mybatis.agent;

import com.htam.agent.common.entity.StudioConfig;
import com.htam.agent.repo.agent.StudioConfigRepository;
import com.htam.agent.repo.mybatis.agent.mapper.StudioConfigMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StudioConfigMybatisRepository implements StudioConfigRepository {
    private final StudioConfigMapper studioConfigMapper;

    @Override
    public List<StudioConfig> list() {
        return studioConfigMapper.selectList(null);
    }

    @Override
    public StudioConfig getById(Long id) {
        return studioConfigMapper.selectById(id);
    }

    @Override
    public boolean save(StudioConfig entity) {
        return studioConfigMapper.insert(entity) > 0;
    }

    @Override
    public boolean updateById(StudioConfig entity) {
        return studioConfigMapper.updateById(entity) > 0;
    }

    @Override
    public boolean deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }
        return studioConfigMapper.deleteBatchIds(ids) >= 0;
    }
}
