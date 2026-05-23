package com.htam.agent.params.core.impl;

import com.htam.agent.common.consts.SysConst;
import com.htam.agent.common.util.FuncUtils;
import com.htam.agent.params.core.ParamsAdapter;
import com.htam.agent.params.core.ParamsCore;
import org.springframework.stereotype.Component;

/**
 * 描述：token存活时长
 *
 * @author huxuehao
 **/
@Component
public class TokenTTLParams implements ParamsCore {
    @Override
    public String checkAndFormatValue(String value) {
        if(FuncUtils.isEmpty(value)) {
            throw new RuntimeException("value值不可为空");
        }
        String trim = value.trim();
        try {
            Long.parseLong(trim);
        } catch (NumberFormatException e) {
            throw new RuntimeException("系统参数ACCESS_TOKEN_TTL不合法",e);
        }
        return trim;
    }

    @Override
    public String getDefaultValue() {
        return String.valueOf(SysConst.ACCESS_TOKEN_TTL);
    }

    @Override
    public void register(ParamsAdapter adapter) {
        adapter.register("ACCESS_TOKEN_TTL", new TokenTTLParams());
    }
}
