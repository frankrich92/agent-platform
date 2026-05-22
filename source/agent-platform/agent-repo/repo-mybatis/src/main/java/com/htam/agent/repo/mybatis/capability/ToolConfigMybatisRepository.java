package com.htam.agent.repo.mybatis.capability;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.htam.agent.common.entity.ToolConfig;
import com.htam.agent.common.enums.ToolType;
import com.htam.agent.common.mp.support.MP;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.repo.capability.ToolConfigRepository;
import com.htam.agent.repo.support.RepoPage;
import com.htam.agent.tool.mapper.ToolMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ToolConfigMybatisRepository implements ToolConfigRepository {
    private final ToolMapper toolMapper;

    @Override
    public RepoPage<ToolConfig> page(PageParams pageParams,
                                     String name,
                                     String toolId,
                                     ToolType toolType,
                                     String category,
                                     Boolean enabled) {
        IPage<ToolConfig> page = toolMapper.selectPage(MP.getPage(pageParams),
                Wrappers.<ToolConfig>lambdaQuery()
                        .like(name != null && !name.isBlank(), ToolConfig::getName, name)
                        .eq(toolId != null && !toolId.isBlank(), ToolConfig::getToolId, toolId)
                        .eq(toolType != null, ToolConfig::getToolType, toolType)
                        .eq(category != null && !category.isBlank(), ToolConfig::getCategory, category)
                        .eq(enabled != null, ToolConfig::getEnabled, enabled));
        return new RepoPage<>(page.getRecords(), page.getTotal(), page.getSize(), page.getCurrent());
    }

    @Override
    public ToolConfig getById(Long id) {
        return toolMapper.selectById(id);
    }

    @Override
    public ToolConfig getByToolId(String toolId) {
        return toolMapper.selectOne(Wrappers.<ToolConfig>lambdaQuery()
                .eq(ToolConfig::getToolId, toolId));
    }

    @Override
    public List<ToolConfig> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return toolMapper.selectBatchIds(ids);
    }

    @Override
    public List<ToolConfig> listEnabledBriefByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return toolMapper.selectList(Wrappers.<ToolConfig>lambdaQuery()
                .select(ToolConfig::getId, ToolConfig::getName, ToolConfig::getToolId, ToolConfig::getDescription)
                .eq(ToolConfig::getEnabled, true)
                .in(ToolConfig::getId, ids));
    }

    @Override
    public List<ToolConfig> listBuiltinByClassPath(String classPath) {
        return toolMapper.selectList(Wrappers.<ToolConfig>lambdaQuery()
                .eq(ToolConfig::getToolType, ToolType.BUILTIN)
                .eq(ToolConfig::getClassPath, classPath));
    }

    @Override
    public boolean save(ToolConfig entity) {
        return toolMapper.insert(entity) > 0;
    }

    @Override
    public boolean updateById(ToolConfig entity) {
        return toolMapper.updateById(entity) > 0;
    }

    @Override
    public boolean updateBuiltinEditableFields(ToolConfig entity) {
        return toolMapper.update(null, Wrappers.<ToolConfig>lambdaUpdate()
                .eq(ToolConfig::getId, entity.getId())
                .set(ToolConfig::getName, entity.getName())
                .set(ToolConfig::getCategory, entity.getCategory())
                .set(ToolConfig::getDescription, entity.getDescription())
                .set(ToolConfig::getNeedConfirm, entity.getNeedConfirm())
                .set(ToolConfig::getVersion, entity.getVersion())) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return toolMapper.deleteById(id) > 0;
    }

    @Override
    public boolean deleteBuiltinNotInClassPaths(List<String> classPaths) {
        if (classPaths == null || classPaths.isEmpty()) {
            return true;
        }
        return toolMapper.delete(Wrappers.<ToolConfig>lambdaQuery()
                .notIn(ToolConfig::getClassPath, classPaths)
                .eq(ToolConfig::getToolType, ToolType.BUILTIN)) >= 0;
    }

    @Override
    public List<String> listCategories() {
        return toolMapper.selectList(Wrappers.<ToolConfig>lambdaQuery()
                        .select(ToolConfig::getCategory)
                        .isNotNull(ToolConfig::getCategory)
                        .groupBy(ToolConfig::getCategory))
                .stream()
                .map(ToolConfig::getCategory)
                .filter(category -> category != null && !category.isEmpty())
                .toList();
    }
}
