package com.htam.agent.core.model;

import com.htam.agent.common.enums.ModelProviderType;
import com.htam.agent.common.wrapper.ModelConfigWrapper;
import io.agentscope.core.model.Model;

/**
 * 描述：聊天模型
 *
 * @author huxuehao
 **/
public interface IChatModel {
    Model getModel(ModelConfigWrapper modelConfig);
    ModelProviderType getProvider();
    int order();
}
