package com.htam.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Agent 平台后端启动入口。
 * 该模块只负责 Spring Boot 应用启动和模块装配，具体业务逻辑分布在 profile、run、capability 等子模块。
 */
@EnableScheduling
@SpringBootApplication
public class Application {
    public static void main( String[] args ) {
        SpringApplication.run(Application.class, args);
    }
}
