package com.htam.agent.websocket.config;

import com.htam.agent.websocket.interceptor.WebSocketAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * 描述：WebSocketConfig
 * 配置 WebSocket 的 endpoint
 *
 * @author huxuehao
 **/
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class AgentWebSocketConfig implements WebSocketConfigurer {
    private final AgentWebSocketHandler agentWebSocketHandler;
    private final WebSocketAuthInterceptor authInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 添加握手拦截器进行认证
        registry.addHandler(agentWebSocketHandler, "/ws/agent")
                .addInterceptors(authInterceptor)
                .setAllowedOrigins("*");
    }
}
