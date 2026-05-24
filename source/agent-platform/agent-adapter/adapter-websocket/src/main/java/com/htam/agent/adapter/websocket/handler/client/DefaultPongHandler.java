package com.htam.agent.adapter.websocket.handler.client;

import com.htam.agent.adapter.websocket.context.AgentWebSocketSession;
import com.htam.agent.common.enums.WsMessageType;
import com.htam.agent.adapter.websocket.model.WsClientMessage;
import org.springframework.stereotype.Service;

/**
 * 描述：客户端向服务端发送PONG类型的消息时的处理器。
 * 维护session健康状态
 *
 * @author huxuehao
 **/
@Service
public class DefaultPongHandler implements ClientMessageHandler {
    @Override
    public WsMessageType messageType() {
        return WsMessageType.PONG;
    }

    @Override
    public void handle(AgentWebSocketSession session, WsClientMessage msg) {
        if (msg == null) {
            return;
        }
        session.setActivateTime(System.currentTimeMillis());
    }

    @Override
    public ClientMessageHandler register() {
        return this;
    }
}
