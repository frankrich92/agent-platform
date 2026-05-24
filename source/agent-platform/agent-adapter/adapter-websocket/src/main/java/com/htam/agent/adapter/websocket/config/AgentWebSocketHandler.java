package com.htam.agent.adapter.websocket.config;

import com.htam.agent.common.UserDetail;
import com.htam.agent.common.consts.SysConst;
import com.htam.agent.common.util.JsonUtils;
import com.htam.agent.adapter.websocket.context.AgentWebSocketSession;
import com.htam.agent.adapter.websocket.handler.ClientMessageHandlerAdapter;
import com.htam.agent.adapter.websocket.handler.client.ClientMessageHandler;
import com.htam.agent.adapter.websocket.model.WsClientMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * 描述：WebSocket 处理器
 *
 * @author huxuehao
 **/
@Slf4j
@Component
public class AgentWebSocketHandler extends TextWebSocketHandler {

    /**
     * 连接建立后
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // 将 WebSocketSession 包装成 AgentWebSocketSession 并进行缓存
        AgentWebSocketSession agentSession = AgentWebSocketSession.from(session);

        // 从 attributes 中获取认证信息并设置
        UserDetail qianmoUser = (UserDetail)session.getAttributes().get(SysConst.USER_DETAIL);

        if (qianmoUser != null) {
            // 设置用户信息
            agentSession.setUser(qianmoUser);

            log.info("WebSocket 连接建立成功：userId={}, username={}",
                    qianmoUser.getId(),
                    qianmoUser.getUsername());
        } else {
            log.warn("WebSocket 连接建立但缺少用户信息：sessionId={}", session.getId());
        }
    }

    /**
     * 链接断开时
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        AgentWebSocketSession agentSession = AgentWebSocketSession.from(session);

        // 从本地缓存移除
        AgentWebSocketSessionManager.remove(agentSession);

        log.info("WebSocket 连接关闭：userId={}, clientId={}, status={}",
            agentSession.getUser() != null ? agentSession.getUser().getId() : "null",
            agentSession.getClientId(), status);
    }

    /**
     * 处理客户端的消息
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            // 刷新会话 TTL
            AgentWebSocketSession agentSession = AgentWebSocketSession.from(session);

            // 将消息负载转成对象
            WsClientMessage messageWrap = JsonUtils.parse(message.getPayload(), WsClientMessage.class);
            // 获取消息处理器
            ClientMessageHandler handler = ClientMessageHandlerAdapter.getHandler(messageWrap.getType());
            if (handler != null) {
                handler.handle(agentSession, messageWrap);
            }

        } catch (Exception e) {
            log.error("处理 WebSocket 消息失败：{}", e.getMessage(), e);
        }
    }
}
