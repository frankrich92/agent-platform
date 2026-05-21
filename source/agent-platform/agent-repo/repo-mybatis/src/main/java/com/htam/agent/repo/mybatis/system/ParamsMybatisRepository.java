package com.htam.agent.repo.mybatis.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.htam.agent.common.entity.Params;
import com.htam.agent.common.mp.support.MP;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.params.mapper.ParamsMapper;
import com.htam.agent.repo.support.RepoPage;
import com.htam.agent.repo.system.ParamsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ParamsMybatisRepository implements ParamsRepository {
    private final ParamsMapper paramsMapper;

    @Override
    public RepoPage<Params> page(PageParams pageParams, String paramName, String paramKey, String paramValue) {
        IPage<Params> page = paramsMapper.selectPage(MP.getPage(pageParams),
                Wrappers.<Params>lambdaQuery()
                        .like(paramName != null && !paramName.isBlank(), Params::getParamName, paramName)
                        .like(paramKey != null && !paramKey.isBlank(), Params::getParamKey, paramKey)
                        .like(paramValue != null && !paramValue.isBlank(), Params::getParamValue, paramValue));
        return new RepoPage<>(page.getRecords(), page.getTotal(), page.getSize(), page.getCurrent());
    }

    @Override
    public Params getById(Long id) {
        return paramsMapper.selectById(id);
    }

    @Override
    public Params getByKey(String key) {
        return paramsMapper.selectOne(Wrappers.<Params>lambdaQuery()
                .eq(Params::getParamKey, key));
    }

    @Override
    public boolean save(Params params) {
        return paramsMapper.insert(params) > 0;
    }

    @Override
    public boolean updateValue(Long id, String value) {
        return paramsMapper.update(null, Wrappers.<Params>lambdaUpdate()
                .eq(Params::getId, id)
                .set(Params::getParamValue, value)) > 0;
    }

    @Override
    public boolean deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        return paramsMapper.deleteBatchIds(ids) > 0;
    }
}
