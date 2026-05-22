package com.htam.agent.common.vo;

import com.htam.agent.common.config.SerializableEnable;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统提示词模板VO
 *
 * @author huxuehao
 */
@Data
@EqualsAndHashCode
public class SystemPromptTemplateVO implements SerializableEnable {
    private Long id;
    private String category;
    private String name;
    private String description;
    private String content;
    private Integer usageCount;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    private List<Object> used;
}
