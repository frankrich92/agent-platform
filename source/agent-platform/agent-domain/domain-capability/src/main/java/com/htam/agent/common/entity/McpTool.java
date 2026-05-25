package com.htam.agent.common.entity;

import com.htam.agent.common.persistence.DomainField;
import com.htam.agent.common.persistence.DomainTable;
import com.fasterxml.jackson.databind.JsonNode;
import com.htam.agent.common.consts.TableConst;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * MCP 工具目录
 *
 * @author huxuehao
 */
@Getter
@Setter
@DomainTable(value = TableConst.MCP_TOOL, autoResultMap = true)
public class McpTool extends BaseEntity {

    /**
     * 所属 MCP 服务 ID
     */
    private Long mcpServerId;

    /**
     * 工具名
     */
    private String toolName;

    /**
     * 工具描述
     */
    private String description;

    /**
     * 输入 Schema
     */
    @DomainField(json = true)
    private JsonNode inputSchema;

    /**
     * 输出 Schema
     */
    @DomainField(json = true)
    private JsonNode outputSchema;

    /**
     * 原始工具 Schema
     */
    @DomainField(json = true)
    private JsonNode rawSchema;

    /**
     * Schema 摘要
     */
    private String schemaHash;

    /**
     * 是否已在当前 MCP 服务中消失
     */
    private Boolean missing;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 首次发现时间
     */
    private LocalDateTime lastDiscoveredAt;

    /**
     * 最近一次发现时间
     */
    private LocalDateTime lastSeenAt;
}
