package com.htam.agent.governance.system.params.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htam.agent.common.consts.RedisChannelTopic;
import com.htam.agent.common.consts.SysConst;
import com.htam.agent.common.entity.Params;
import com.htam.agent.common.message.ParamChangeMessage;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.common.util.FuncUtils;
import com.htam.agent.common.util.JsonUtils;
import com.htam.agent.governance.system.params.core.ParamsAdapter;
import com.htam.agent.repo.support.RepoPage;
import com.htam.agent.repo.system.ParamsRepository;
import com.htam.agent.run.event.cluster.core.MessagePublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 描述：系统参数
 *
 * @author huxuehao
 **/
@Slf4j
@Service
public class ParamsServiceImpl implements ParamsService {
    private final ParamsAdapter paramsAdapter;
    private final ParamsRepository paramsRepository;
    private final MessagePublisher messagePublisher;

    public ParamsServiceImpl(ParamsAdapter paramsAdapter, ParamsRepository paramsRepository, MessagePublisher messagePublisher) {
        this.paramsAdapter = paramsAdapter;
        this.paramsRepository = paramsRepository;
        this.messagePublisher = messagePublisher;
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

        boolean result = paramsAdapter.saveParams(params);
        publishParamChange(params.getParamKey());
        return result;
    }


    @Override
    public boolean updateByIdV2(Params params) {
        if (FuncUtils.isEmpty(params.getParamValue())) {
            throw new RuntimeException("value值不可为空");
        }

        boolean result = paramsAdapter.updateParams(params);
        publishParamChange(params.getParamKey());
        return result;
    }

    private void publishParamChange(String paramKey) {
        try {
            ParamChangeMessage message = ParamChangeMessage.create(SysConst.CURRENT_NODE_ID, paramKey);
            String json = JsonUtils.toJsonStr(message);
            if (json != null) {
                messagePublisher.publishAfterCommit(RedisChannelTopic.PARAM_CHANGE_CHANNEL, json);
                log.debug("发布参数变更广播 - paramKey: {}", paramKey);
            }
        } catch (Exception e) {
            log.error("发布参数变更广播失败", e);
        }
    }
}
