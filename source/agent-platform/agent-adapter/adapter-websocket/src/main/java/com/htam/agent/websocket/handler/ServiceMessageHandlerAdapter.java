package com.htam.agent.websocket.handler;

import com.htam.agent.common.enums.WsMessageType;
import com.htam.agent.websocket.handler.server.ServerMessageHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 描述：
 *
 * @author huxuehao
 **/
public class ServiceMessageHandlerAdapter {
    private static final Map<WsMessageType, ServerMessageHandler> CACHED = new ConcurrentHashMap<>();


    public static void register(ServerMessageHandler handler) {
        CACHED.put(handler.messageType(), handler);
    }

    public static ServerMessageHandler getHandler(WsMessageType type) {
        return CACHED.get(type);
    }
}
