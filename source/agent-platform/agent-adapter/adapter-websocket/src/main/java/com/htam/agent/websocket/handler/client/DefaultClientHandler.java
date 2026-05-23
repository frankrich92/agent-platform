package com.htam.agent.websocket.handler.client;

import com.htam.agent.common.enums.WsMessageType;
import com.htam.agent.websocket.context.AgentWebSocketSession;
import com.htam.agent.websocket.model.WsClientMessage;
import org.springframework.stereotype.Service;

/**
 * 描述：客户端向服务端发送PONG类型的消息时的处理器。
 * 维护session健康状态
 *
 * @author huxuehao
 **/
@Service
public class DefaultClientHandler implements ClientMessageHandler {
    @Override
    public WsMessageType messageType() {
        return WsMessageType.CLIENT;
    }

    @Override
    public void handle(AgentWebSocketSession session, WsClientMessage msg) {
        if (msg == null) {
            return;
        }
        session.setClientId(msg.getContent().toString());
    }

    @Override
    public ClientMessageHandler register() {
        return this;
    }
}
