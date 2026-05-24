package com.htam.agent.runtime.agentscope.tool.dynamices;

import com.htam.agent.runtime.agentscope.InstanceLoader;

/**
 * 描述：实例加载接口
 *
 * @author huxuehao
 **/
public interface ToolInstanceLoader extends InstanceLoader<IDynamicAgentTool> {
    /**
     * 初始化
     */
    @Override
    default void afterSingletonsInstantiated() {
        // 完成注册
        ToolInstanceLoadFactory.registerLoader(this);
    };
}
