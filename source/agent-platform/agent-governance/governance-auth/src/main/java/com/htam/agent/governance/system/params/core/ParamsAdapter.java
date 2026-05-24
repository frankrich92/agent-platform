package com.htam.agent.governance.system.params.core;

import com.htam.agent.common.entity.Params;
import com.htam.agent.repo.system.ParamsRepository;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 描述：参数校验适配器
 *
 * @author huxuehao
 **/
@Component
public class ParamsAdapter {
    // 注册表
    private static final Map<String, ParamsCore> registryMap = new ConcurrentHashMap<>();

    private final ParamsRepository paramsRepository;
    public ParamsAdapter(ParamsRepository paramsRepository) {
        this.paramsRepository = paramsRepository;
    }

    public void register(String key, ParamsCore paramsCore) {
        assert key != null : "transferEnum 不可为空";
        registryMap.put(key, paramsCore);
    }

    public String getValue(String key) {
        ParamsCore paramsCore = registryMap.get(key);
        try {
            Params params = paramsRepository.getByKey(key);
            if (params == null) {
                return paramsCore == null ? null : paramsCore.getDefaultValue();
            }
            if (paramsCore == null) {
                return params.getParamValue();
            }
            return paramsCore.checkAndFormatValue(params.getParamValue());
        } catch (Exception e) {
            throw new RuntimeException("不存在唯一Key:" + key, e);
        }
    }
    public boolean saveParams(Params params) {
        ParamsCore paramsCore = registryMap.get(params.getParamKey());
        if (paramsCore != null) {
            params.setParamValue(paramsCore.checkAndFormatValue(params.getParamValue()));
        }
        return paramsRepository.save(params);
    }

    public boolean updateParams(Params params) {
        ParamsCore paramsCore = registryMap.get(params.getParamKey());
        if (paramsCore != null) {
            params.setParamValue(paramsCore.checkAndFormatValue(params.getParamValue()));
        }
        return paramsRepository.updateValue(params.getId(), params.getParamValue());
    }
}
