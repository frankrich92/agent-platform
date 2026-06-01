package com.htam.agent.runtime.agentscope.model.impl;

import com.htam.agent.common.enums.ModelProviderType;
import com.htam.agent.common.wrapper.ModelConfigWrapper;
import com.htam.agent.runtime.agentscope.formatter.FixedSysMsgOpenAIChatFormatter;
import com.htam.agent.runtime.agentscope.formatter.FixedSysMsgOpenAIMultiAgentFormatter;
import com.htam.agent.runtime.agentscope.model.IChatModel;
import com.htam.agent.runtime.agentscope.model.GenerateOptionsHelper;
import com.htam.agent.runtime.agentscope.model.HttpTransportHelper;
import io.agentscope.core.formatter.openai.OpenAIChatFormatter;
import io.agentscope.core.formatter.openai.OpenAIMultiAgentFormatter;
import io.agentscope.core.model.Model;
import io.agentscope.core.model.OpenAIChatModel;
import org.springframework.stereotype.Component;

/**
 * 描述：OpenAI 模型
 *
 * @author huxuehao
 **/
@Component
public class DefaultOpenAIModelI implements IChatModel {
    @Override
    public Model getModel(ModelConfigWrapper config) {
        if (config.getProvider() != getProvider()) {
            throw new IllegalArgumentException("The provider is not supported");
        }

        OpenAIChatModel.Builder builder = OpenAIChatModel.builder()
                .apiKey(config.getApiKey())
                .modelName(config.getModelCode())
                .stream(config.getStreaming() != null && config.getStreaming())
                .httpTransport(HttpTransportHelper.createOkHttpTransport())
                .generateOptions(GenerateOptionsHelper.create(config));

        if (config.getBaseUrl() != null && !config.getBaseUrl().isEmpty()) {
            builder.baseUrl(config.getBaseUrl());
        }

        if (config.isMulti()) {
            if (config.getFixedSystemMessage() != null && config.getFixedSystemMessage()) {
                builder.formatter(new FixedSysMsgOpenAIMultiAgentFormatter());
            } else {
                builder.formatter(new OpenAIMultiAgentFormatter());
            }

        } else {
            if (config.getFixedSystemMessage() != null && config.getFixedSystemMessage()) {
                builder.formatter(new FixedSysMsgOpenAIChatFormatter());
            } else {
                builder.formatter(new OpenAIChatFormatter());
            }
        }

        return builder.build();
    }

    @Override
    public Model getSimpleModel(ModelConfigWrapper config) {
        if (config.getProvider() != getProvider()) {
            throw new IllegalArgumentException("The provider is not supported");
        }

        OpenAIChatModel.Builder builder = OpenAIChatModel.builder()
                .apiKey(config.getApiKey())
                .modelName(config.getModelCode())
                .stream(config.getStreaming() != null && config.getStreaming())
                .httpTransport(HttpTransportHelper.createOkHttpTransport(10, 15));

        if (config.getBaseUrl() != null && !config.getBaseUrl().isEmpty()) {
            builder.baseUrl(config.getBaseUrl());
        }

        return builder.build();
    }

    @Override
    public ModelProviderType getProvider() {
        return ModelProviderType.OPEN_AI;
    }

    @Override
    public int order() {
        return 0;
    }
}
