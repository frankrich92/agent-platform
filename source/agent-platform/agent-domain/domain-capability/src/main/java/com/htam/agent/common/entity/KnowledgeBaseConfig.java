package com.htam.agent.common.entity;

import com.htam.agent.common.consts.TableConst;
import com.htam.agent.common.enums.HealthStatus;
import com.htam.agent.common.enums.KbType;
import com.htam.agent.common.enums.RagMode;
import com.htam.agent.common.persistence.DomainField;
import com.htam.agent.common.persistence.DomainTable;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 知识库配置
 *
 * @author huxuehao
 */
@Getter
@Setter
@DomainTable(value = TableConst.KNOWLEDGE, autoResultMap = true)
public class KnowledgeBaseConfig extends BaseEntity {

    /**
     * 知识库名称
     */
    private String name;

    /**
     * 知识库类型
     */
    private KbType kbType;

    /**
     * 集成模式
     */
    private RagMode ragMode;

    /**
     * 描述
     */
    private String description;

    /**
     * 连接配置
     */
    @DomainField(json = true)
    private JsonNode connectionConfig;

    /**
     * 端点配置
     */
    @DomainField(json = true)
    private JsonNode endpointConfig;

    /**
     * 检索配置
     */
    @DomainField(json = true)
    private JsonNode retrievalConfig;

    /**
     * 重排序配置
     */
    @DomainField(json = true)
    private JsonNode rerankingConfig;

    /**
     * 查询重写配置
     */
    @DomainField(json = true)
    private JsonNode queryRewriteConfig;

    /**
     * 元数据过滤
     */
    @DomainField(json = true)
    private JsonNode metadataFilters;

    /**
     * HTTP配置
     */
    @DomainField(json = true)
    private JsonNode httpConfig;

    /**
     * 健康状态
     */
    private HealthStatus healthStatus;

    /**
     * 最后同步时间
     */
    private LocalDateTime lastSyncTime;
}
