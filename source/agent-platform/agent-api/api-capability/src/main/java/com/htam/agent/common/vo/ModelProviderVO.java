package com.htam.agent.common.vo;

import com.htam.agent.common.config.SerializableEnable;
import com.htam.agent.common.enums.AuthType;
import com.fasterxml.jackson.databind.JsonNode;
import com.htam.agent.common.enums.ModelProviderType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 模型提供商VO
 *
 * @author huxuehao
 */
@Data
@EqualsAndHashCode
public class ModelProviderVO implements SerializableEnable {
    private Long id;
    private ModelProviderType type;
    private String name;
    private String description;
    private String baseUrl;
    private AuthType authType;
    private String apiKey;
    private String envVarName;
    private Boolean enabled;
    private JsonNode configMeta;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
}
