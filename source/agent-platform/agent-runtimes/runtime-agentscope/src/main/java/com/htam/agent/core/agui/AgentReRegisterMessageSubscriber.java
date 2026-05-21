package com.htam.agent.core.agui;

import com.htam.agent.agent.service.AgentDefinitionService;
import com.htam.agent.cluster.core.ChannelSubscriber;
import com.htam.agent.common.consts.RedisChannelTopic;
import com.htam.agent.common.entity.AgentDefinition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.Topic;
import org.springframework.stereotype.Component;

/**
 * 描述：edis 消息订阅者 - 仅处理 apboa:agent:cluster:reRegister 频道的跨节点消息
 *
 * @author huxuehao
 **/
@Component
@RequiredArgsConstructor
public class AgentReRegisterMessageSubscriber implements ChannelSubscriber {


    private final AgentDefinitionService agentDefinitionService;
    private final AguiAgentConfiguration aguiAgentConfiguration;

    @Override
    public Topic getTopic() {
        return new ChannelTopic(RedisChannelTopic.AGENT_REREGISTER_CHANNEL);
    }

    @Override
    public void onMessage(String channel, String message) {
        if (!channel.equals(RedisChannelTopic.AGENT_REREGISTER_CHANNEL)) {
            return;
        }

        AgentDefinition agentDefinition = agentDefinitionService.getById(message);
        if (agentDefinition != null) {
            aguiAgentConfiguration.reRegisterAgent(agentDefinition);
        }
    }
}
