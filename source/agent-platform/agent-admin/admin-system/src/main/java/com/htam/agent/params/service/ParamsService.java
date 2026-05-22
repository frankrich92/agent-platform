package com.htam.agent.params.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.htam.agent.common.entity.Params;
import com.htam.agent.common.mp.support.PageParams;

import java.util.List;

/**
 * 描述：系统参数
 *
 * @author huxuehao
 **/
public interface ParamsService {
    IPage<Params> page(PageParams pageParams, Params query);

    boolean removeBatchByIds(List<Long> ids);

    Params getById(Long id);

    String fetchValueByKey(String key);

    boolean saveV2(Params params);

    boolean updateByIdV2(Params params);
}
