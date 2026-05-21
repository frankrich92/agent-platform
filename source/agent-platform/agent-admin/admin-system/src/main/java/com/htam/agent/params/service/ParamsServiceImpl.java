package com.htam.agent.params.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htam.agent.common.entity.Params;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.common.util.FuncUtils;
import com.htam.agent.params.core.ParamsAdapter;
import com.htam.agent.repo.support.RepoPage;
import com.htam.agent.repo.system.ParamsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 描述：系统参数
 *
 * @author huxuehao
 **/
@Service
public class ParamsServiceImpl implements ParamsService {
    private final ParamsAdapter paramsAdapter;
    private final ParamsRepository paramsRepository;

    public ParamsServiceImpl(ParamsAdapter paramsAdapter, ParamsRepository paramsRepository) {
        this.paramsAdapter = paramsAdapter;
        this.paramsRepository = paramsRepository;
    }

    @Override
    public IPage<Params> page(PageParams pageParams, Params query) {
        Params params = query == null ? new Params() : query;
        RepoPage<Params> repoPage = paramsRepository.page(
                pageParams,
                params.getParamName(),
                params.getParamKey(),
                params.getParamValue());
        IPage<Params> page = new Page<>(repoPage.current(), repoPage.size(), repoPage.total());
        page.setRecords(repoPage.records());
        return page;
    }

    @Override
    public boolean removeBatchByIds(List<Long> ids) {
        return paramsRepository.deleteByIds(ids);
    }

    @Override
    public Params getById(Long id) {
        return paramsRepository.getById(id);
    }

    @Override
    public String fetchValueByKey(String key) {
        return paramsAdapter.getValue(key);
    }

    @Override
    public boolean saveV2(Params params) {
        if (FuncUtils.isEmpty(params.getParamValue())) {
            throw new RuntimeException("value值不可为空");
        }

        Params existing = paramsRepository.getByKey(params.getParamKey());
        if (existing != null) {
            throw new RuntimeException("Key已存在");
        }

        return paramsAdapter.saveParams(params);
    }


    @Override
    public boolean updateByIdV2(Params params) {
        if (FuncUtils.isEmpty(params.getParamValue())) {
            throw new RuntimeException("value值不可为空");
        }

        return paramsAdapter.updateParams(params);
    }
}
