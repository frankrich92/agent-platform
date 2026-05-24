package com.htam.agent.repo.mybatis.capability;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.htam.agent.common.entity.HookConfig;
import com.htam.agent.common.enums.HookType;
import com.htam.agent.common.mp.support.MP;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.repo.mybatis.capability.mapper.HookConfigMapper;
import com.htam.agent.repo.capability.HookConfigRepository;
import com.htam.agent.repo.support.RepoPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class HookConfigMybatisRepository implements HookConfigRepository {
    private final HookConfigMapper hookConfigMapper;

    @Override
    public RepoPage<HookConfig> page(PageParams pageParams, String name, HookType hookType, Boolean enabled) {
        IPage<HookConfig> page = hookConfigMapper.selectPage(MP.getPage(pageParams),
                Wrappers.<HookConfig>lambdaQuery()
                        .like(name != null && !name.isBlank(), HookConfig::getName, name)
                        .eq(hookType != null, HookConfig::getHookType, hookType)
                        .eq(enabled != null, HookConfig::getEnabled, enabled));
        return new RepoPage<>(page.getRecords(), page.getTotal(), page.getSize(), page.getCurrent());
    }

    @Override
    public HookConfig getById(Long id) {
        return hookConfigMapper.selectById(id);
    }

    @Override
    public List<HookConfig> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return hookConfigMapper.selectBatchIds(ids);
    }

    @Override
    public List<HookConfig> listByClassPath(String classPath) {
        return hookConfigMapper.selectList(Wrappers.<HookConfig>lambdaQuery()
                .eq(HookConfig::getClassPath, classPath));
    }

    @Override
    public boolean save(HookConfig entity) {
        return hookConfigMapper.insert(entity) > 0;
    }

    @Override
    public boolean updateById(HookConfig entity) {
        return hookConfigMapper.updateById(entity) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return hookConfigMapper.deleteById(id) > 0;
    }

    @Override
    public boolean deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }
        return hookConfigMapper.deleteBatchIds(ids) > 0;
    }

    @Override
    public boolean deleteBuiltinNotInClassPaths(List<String> classPaths) {
        if (classPaths == null || classPaths.isEmpty()) {
            return true;
        }
        return hookConfigMapper.delete(Wrappers.<HookConfig>lambdaQuery()
                .notIn(HookConfig::getClassPath, classPaths)
                .isNotNull(HookConfig::getClassPath)) >= 0;
    }
}
