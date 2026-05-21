package com.htam.agent.core.hook.dynamices;

import com.htam.agent.core.InstanceLoader;
import io.agentscope.core.hook.Hook;

/**
 * 描述：HookInstanceLoader
 *
 * @author huxuehao
 **/
public interface HookInstanceLoader extends InstanceLoader<Hook> {
    /**
     * 初始化
     */
    @Override
    default void afterSingletonsInstantiated() {
        // 完成注册
        HookInstanceLoadFactory.registerLoader(this);
    };
}
