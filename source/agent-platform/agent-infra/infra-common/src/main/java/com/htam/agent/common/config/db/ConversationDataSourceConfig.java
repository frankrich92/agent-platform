package com.htam.agent.common.config.db;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.htam.agent.common.consts.DataSourceConst;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Conversation datasource aliases for plain JDBC code paths.
 */
@Configuration
@ConditionalOnProperty(name = "spring.datasource.dynamic.enabled", havingValue = "true")
public class ConversationDataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(ConversationDataSourceConfig.class);

    @Bean(name = "conversationDataSource")
    public DataSource conversationDataSource(DynamicRoutingDataSource dynamicRoutingDataSource) {
        DataSource dataSource = dynamicRoutingDataSource.getDataSource(DataSourceConst.CONVERSATION);
        if (dataSource == null) {
            throw new BeanCreationException("conversationDataSource", "Dynamic datasource 'conversation' is not configured");
        }
        log.info("Conversation数据源初始化完成, name={}", DataSourceConst.CONVERSATION);
        return dataSource;
    }

    @Bean(name = "conversationJdbcTemplate")
    public JdbcTemplate conversationJdbcTemplate(@Qualifier("conversationDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
