package com.htam.agent.websocket.handler.server;

import com.htam.agent.websocket.context.AgentWebSocketSession;
import com.htam.agent.common.enums.WsMessageType;
import com.htam.agent.websocket.handler.ServiceMessageHandlerAdapter;
import com.htam.agent.websocket.model.WsServerMessage;
import org.springframework.beans.factory.SmartInitializingSingleton;

/**
 * 描述：服务端 websocket 消息处理器
 *
 * @author huxuehao
 **/
public interface ServerMessageHandler extends SmartInitializingSingleton  {
    /**
     * 可以处理的消息类型
     */
    WsMessageType messageType();

    /**
     * 处理消息的执行方法
     *
     * @param session AgentWebSocketSession
     * @param msg     消息
     */
    void handle(AgentWebSocketSession session, WsServerMessage msg);

    /**
     * 注册实现类的Handler
     */
    ServerMessageHandler register();


    @Override
    default void afterSingletonsInstantiated() {
        ServiceMessageHandlerAdapter.register(register());
    }
}
