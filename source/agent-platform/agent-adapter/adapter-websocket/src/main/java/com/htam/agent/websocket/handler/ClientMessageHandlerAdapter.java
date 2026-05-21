package com.htam.agent.websocket.handler;

import com.htam.agent.common.enums.WsMessageType;
import com.htam.agent.websocket.handler.client.ClientMessageHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 描述：websocket 消息处理器适配器
 *
 * @author huxuehao
 **/
public class ClientMessageHandlerAdapter {
    private static final Map<WsMessageType, ClientMessageHandler> CACHED = new ConcurrentHashMap<>();


    public static void register(ClientMessageHandler handler) {
        CACHED.put(handler.messageType(), handler);
    }

    public static ClientMessageHandler getHandler(WsMessageType type) {
        return CACHED.get(type);
    }
}
