package com.htam.agent.websocket.cluster;

import com.htam.agent.cluster.core.ChannelSubscriber;
import com.htam.agent.common.consts.RedisChannelTopic;
import com.htam.agent.common.util.JsonUtils;
import com.htam.agent.websocket.config.AgentWebSocketSessionManager;
import com.htam.agent.websocket.context.AgentWebSocketSession;
import com.htam.agent.websocket.model.WsServerMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.Topic;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 描述：Redis 消息订阅者 - 仅处理 apboa:ws:cluster:* 频道的跨节点消息
 *
 * @author huxuehao
 **/
@Slf4j
@Component
public class ClusterMessageSubscriber implements ChannelSubscriber {

    @Override
    public Topic getTopic() {
        return new PatternTopic(RedisChannelTopic.WS_CHANNEL_PATTERN);
    }

    @Override
    public void onMessage(String channel, String message) {
        try {
            // 仅处理 apboa:ws:cluster:* 频道，拒绝非 WebSocket 集群消息
            String subChannel = RedisChannelTopic.WS_CHANNEL_PATTERN.substring(0, RedisChannelTopic.WS_CHANNEL_PATTERN.length() - 1);
            if (!channel.startsWith(subChannel)) {
                log.warn("忽略非 WebSocket 集群频道的消息：channel={}", channel);
                return;
            }

            log.debug("接收到集群消息：channel={}", channel);

            ClusterMessage clusterMessage = JsonUtils.parse(message, ClusterMessage.class);

            // 构建消息对象
            WsServerMessage wsMessage = new WsServerMessage(
                    clusterMessage.getMessageType(),
                    clusterMessage.getContent()
            );

            // 按 clientId 精准推送
            if (clusterMessage.getTargetClientId() != null) {
                AgentWebSocketSession session = AgentWebSocketSession.getSessionByClientId(clusterMessage.getTargetClientId());
                if (session != null && session.writeable()) {
                    AgentWebSocketSessionManager.sendBySession(session, wsMessage);
                    log.info("集群消息转发成功：targetClientId={}", clusterMessage.getTargetClientId());
                } else {
                    log.debug("目标客户端不在本节点：targetClientId={}", clusterMessage.getTargetClientId());
                }
            }
            // 按 userId 推送
            else if (clusterMessage.getUserId() != null) {
                List<AgentWebSocketSession> sessions = AgentWebSocketSession.getSessionByAccountId(clusterMessage.getUserId());
                for (AgentWebSocketSession session : sessions) {
                    if (session != null && session.writeable()) {
                        AgentWebSocketSessionManager.sendBySession(session, wsMessage);
                    }
                }
                log.info("集群消息转发成功：userId={}, sessionCount={}", clusterMessage.getUserId(), sessions.size());
            }
            // 全广播
            else {
                if (clusterMessage.getExcludeClientId() != null) {
                    AgentWebSocketSessionManager.sendToOther(clusterMessage.getExcludeClientId(), wsMessage);
                } else {
                    AgentWebSocketSessionManager.sendToAll(wsMessage);
                }
                log.info("集群广播消息转发成功：messageType={}", clusterMessage.getMessageType());
            }
        } catch (Exception e) {
            log.error("处理集群消息失败：{}", e.getMessage(), e);
        }
    }
}
