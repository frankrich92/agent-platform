package com.htam.agent.common.config.db;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.provider.DynamicDataSourceProvider;
import com.htam.agent.common.consts.DataSourceConst;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Dynamic PostgreSQL datasource configuration.
 */
@Configuration
@ConditionalOnProperty(name = "spring.datasource.dynamic.enabled", havingValue = "true")
public class DynamicPostgreSqlDataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DynamicPostgreSqlDataSourceConfig.class);

    @Bean
    @Primary
    public DynamicRoutingDataSource dataSource(
            @Value("${spring.datasource.dynamic.datasource.master.url}") String masterUrl,
            @Value("${spring.datasource.dynamic.datasource.master.username}") String masterUsername,
            @Value("${spring.datasource.dynamic.datasource.master.password}") String masterPassword,
            @Value("${spring.datasource.dynamic.datasource.master.driver-class-name}") String masterDriverClassName,
            @Value("${spring.datasource.dynamic.datasource.conversation.url}") String conversationUrl,
            @Value("${spring.datasource.dynamic.datasource.conversation.username}") String conversationUsername,
            @Value("${spring.datasource.dynamic.datasource.conversation.password}") String conversationPassword,
            @Value("${spring.datasource.dynamic.datasource.conversation.driver-class-name}") String conversationDriverClassName,
            @Value("${spring.datasource.dynamic.druid.initial-size:5}") int initialSize,
            @Value("${spring.datasource.dynamic.druid.min-idle:5}") int minIdle,
            @Value("${spring.datasource.dynamic.druid.max-active:20}") int maxActive,
            @Value("${spring.datasource.dynamic.druid.max-wait:60000}") long maxWait,
            @Value("${spring.datasource.dynamic.druid.validation-query:SELECT 1}") String validationQuery,
            @Value("${spring.datasource.dynamic.druid.test-on-borrow:false}") boolean testOnBorrow,
            @Value("${spring.datasource.dynamic.druid.test-on-return:false}") boolean testOnReturn,
            @Value("${spring.datasource.dynamic.druid.test-while-idle:true}") boolean testWhileIdle,
            @Value("${spring.datasource.dynamic.druid.time-between-eviction-runs-millis:60000}") long timeBetweenEvictionRunsMillis,
            @Value("${spring.datasource.dynamic.druid.min-evictable-idle-time-millis:300000}") long minEvictableIdleTimeMillis) {

        Map<String, DataSource> dataSources = new LinkedHashMap<>();
        dataSources.put(DataSourceConst.MASTER, buildDruidDataSource(
                masterUrl,
                masterUsername,
                masterPassword,
                masterDriverClassName,
                initialSize,
                minIdle,
                maxActive,
                maxWait,
                validationQuery,
                testOnBorrow,
                testOnReturn,
                testWhileIdle,
                timeBetweenEvictionRunsMillis,
                minEvictableIdleTimeMillis));
        dataSources.put(DataSourceConst.CONVERSATION, buildDruidDataSource(
                conversationUrl,
                conversationUsername,
                conversationPassword,
                conversationDriverClassName,
                initialSize,
                minIdle,
                maxActive,
                maxWait,
                validationQuery,
                testOnBorrow,
                testOnReturn,
                testWhileIdle,
                timeBetweenEvictionRunsMillis,
                minEvictableIdleTimeMillis));

        DynamicDataSourceProvider provider = () -> dataSources;
        DynamicRoutingDataSource routingDataSource = new DynamicRoutingDataSource(List.of(provider));
        routingDataSource.setPrimary(DataSourceConst.MASTER);
        routingDataSource.setStrict(true);
        log.info(
                "Dynamic PostgreSQL数据源初始化完成, primary={}, datasources={}",
                DataSourceConst.MASTER,
                dataSources.keySet());
        return routingDataSource;
    }

    private DruidDataSource buildDruidDataSource(
            String url,
            String username,
            String password,
            String driverClassName,
            int initialSize,
            int minIdle,
            int maxActive,
            long maxWait,
            String validationQuery,
            boolean testOnBorrow,
            boolean testOnReturn,
            boolean testWhileIdle,
            long timeBetweenEvictionRunsMillis,
            long minEvictableIdleTimeMillis) {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);
        dataSource.setInitialSize(initialSize);
        dataSource.setMinIdle(minIdle);
        dataSource.setMaxActive(maxActive);
        dataSource.setMaxWait(maxWait);
        dataSource.setValidationQuery(validationQuery);
        dataSource.setTestOnBorrow(testOnBorrow);
        dataSource.setTestOnReturn(testOnReturn);
        dataSource.setTestWhileIdle(testWhileIdle);
        dataSource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        dataSource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        return dataSource;
    }
}
