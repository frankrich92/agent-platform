package com.htam.agent.runtime.agentscope.model;

import io.agentscope.core.model.transport.HttpTransport;
import io.agentscope.core.model.transport.HttpTransportConfig;
import io.agentscope.core.model.transport.JdkHttpTransport;
import io.agentscope.core.model.transport.OkHttpTransport;

import java.time.Duration;

/**
 * 描述：HttpTransport 生成器
 *
 * @author huxuehao
 **/
public class HttpTransportHelper {
    public static HttpTransport createJdkHttpTransport() {
        return JdkHttpTransport.builder()
                .config(getHttpTransportConfig())
                .build();
    }

    public static HttpTransport createOkHttpTransport() {
        return OkHttpTransport.builder()
                .config(getHttpTransportConfig())
                .build();
    }

    public static HttpTransport createOkHttpTransport(long connectTimeoutSeconds, long readTimeoutSeconds) {
        return OkHttpTransport.builder()
                .config(getHttpTransportConfig(connectTimeoutSeconds, readTimeoutSeconds))
                .build();
    }

    private static HttpTransportConfig getHttpTransportConfig() {
        return getHttpTransportConfig(10, 60);
    }

    private static HttpTransportConfig getHttpTransportConfig(long connectTimeoutSeconds, long readTimeoutSeconds) {
        return HttpTransportConfig.builder()
                .connectTimeout(Duration.ofSeconds(connectTimeoutSeconds))
                .readTimeout(Duration.ofSeconds(readTimeoutSeconds))
                .ignoreSsl(true)
                .build();
    }
}
