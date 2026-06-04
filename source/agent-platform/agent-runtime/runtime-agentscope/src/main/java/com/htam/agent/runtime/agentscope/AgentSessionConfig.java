package com.htam.agent.runtime.agentscope;

import com.htam.agent.common.consts.TableConst;
import io.agentscope.core.agui.adapter.AguiAdapterConfig;
import io.agentscope.core.agui.observer.AguiRunEventObserver;
import io.agentscope.core.agui.observer.AguiRequestUserProvider;
import io.agentscope.core.agui.registry.AguiAgentRegistry;
import io.agentscope.core.session.Session;
import io.agentscope.spring.boot.agui.common.AguiProperties;
import io.agentscope.spring.boot.agui.common.ThreadSessionManager;
import io.agentscope.spring.boot.agui.mvc.AguiMvcEndpoint;
import io.agentscope.spring.boot.agui.webflux.AguiWebFluxHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * 配置 PostgreSQL Session 替代 InMemorySession，实现状态持久化
 *
 * @author huxuehao
 */
@Slf4j
@Configuration
@ConditionalOnClass(DataSource.class)
public class AgentSessionConfig {

    /**
     * 创建 PostgreSQL Session Bean
     *
     * @param dataSource 数据源
     * @return PostgreSQL Session 实例
     */
    @Bean
    @Primary
    public Session agentSession(
            DataSource dataSource,
            @Qualifier("conversationDataSource") ObjectProvider<DataSource> conversationDataSource) {
        return new PostgresSession(
                conversationDataSource.getIfAvailable(() -> dataSource),
                TableConst.AGENT_SCOPE_SESSIONS);
    }

    /**
     * 配置 AguiMvcEndpoint
     * 覆盖 agentscope-agui-spring-boot-starter 的默认配置
     */
    @Bean
    @Primary
    @ConditionalOnClass(name = "io.agentscope.spring.boot.agui.mvc.AguiMvcEndpoint")
    public AguiMvcEndpoint aguiMvcEndpoint(
            @Autowired JdbcTemplate jdbcTemplate,
            @Qualifier("conversationJdbcTemplate") ObjectProvider<JdbcTemplate> conversationJdbcTemplate,
            @Autowired(required = false) AguiAgentRegistry registry,
            @Autowired(required = false) ThreadSessionManager sessionManager,
            ObjectProvider<AguiRunEventObserver> eventObservers,
            ObjectProvider<AguiRequestUserProvider> requestUserProvider,
            AguiProperties props,
        Session session) {

        if (registry == null) {
            log.warn("AguiAgentRegistry not found, skip AguiMvcEndpoint configuration");
            return null;
        }

        if (session != null) {
            registry.setSessionManager(sessionManager);
        }

        return AguiMvcEndpoint.builder()
                .agentRegistry(registry)
                .sessionManager(sessionManager)
                .serverSideMemory(props.isServerSideMemory())
                .session(session)
                .jdbcTemplate(jdbcTemplate)
                .conversationJdbcTemplate(conversationJdbcTemplate.getIfAvailable(() -> jdbcTemplate))
                .eventObservers(eventObservers.orderedStream().toList())
                .requestUserProvider(requestUserProvider.getIfAvailable())
                .sseTimeout(600000L)
                .config(buildAguiAdapterConfig(props))
                .build();
    }

    /**
     * 配置 AguiWebFluxHandler
     * 覆盖 agentscope-agui-spring-boot-starter 的默认配置
     */
    @Bean
    @Primary
    @ConditionalOnClass(name = "io.agentscope.spring.boot.agui.webflux.AguiWebFluxHandler")
    public AguiWebFluxHandler aguiWebFluxHandler(
            @Autowired JdbcTemplate jdbcTemplate,
            @Qualifier("conversationJdbcTemplate") ObjectProvider<JdbcTemplate> conversationJdbcTemplate,
            @Autowired(required = false) AguiAgentRegistry registry,
            @Autowired(required = false) ThreadSessionManager sessionManager,
            AguiProperties props,
        Session session) {

        if (registry == null) {
            log.warn("AguiAgentRegistry not found, skip AguiWebFluxHandler configuration");
            return null;
        }

        if (session != null) {
            registry.setSessionManager(sessionManager);
        }

        return AguiWebFluxHandler.builder()
                .agentRegistry(registry)
                .sessionManager(sessionManager)
                .serverSideMemory(props.isServerSideMemory())
                .session(session)
                .jdbcTemplate(jdbcTemplate)
                .conversationJdbcTemplate(conversationJdbcTemplate.getIfAvailable(() -> jdbcTemplate))
                .config(buildAguiAdapterConfig(props))
                .build();
    }

    private AguiAdapterConfig buildAguiAdapterConfig(AguiProperties props) {
        return AguiAdapterConfig.builder()
                .toolMergeMode(props.getDefaultToolMergeMode())
                .runTimeout(props.getRunTimeout())
                .emitStateEvents(props.isEmitStateEvents())
                .emitToolCallArgs(props.isEmitToolCallArgs())
                .enableReasoning(props.isEnableReasoning())
                .defaultAgentId(props.getDefaultAgentId())
                .build();
    }
}
