package com.htam.agent.runtime.agentscope.agui;

import com.htam.agent.run.event.cluster.core.ChannelSubscriber;
import com.htam.agent.common.consts.RedisChannelTopic;
import com.htam.agent.common.entity.AgentDefinition;
import com.htam.agent.repo.agent.AgentDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.Topic;
import org.springframework.stereotype.Component;

/**
 * 描述：Redis 消息订阅者 - 仅处理 agent:cluster:reRegister 频道的跨节点消息
 *
 * @author huxuehao
 **/
@Component
@RequiredArgsConstructor
public class AgentReRegisterMessageSubscriber implements ChannelSubscriber {


    private final AgentDefinitionRepository agentDefinitionRepository;
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

        AgentDefinition agentDefinition = agentDefinitionRepository.getById(Long.valueOf(message));
        if (agentDefinition != null) {
            aguiAgentConfiguration.reRegisterAgent(agentDefinition);
        }
    }
}
