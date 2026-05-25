package com.htam.agent.common.entity;

import com.htam.agent.common.persistence.DomainIdType;
import com.htam.agent.common.persistence.DomainId;
import com.htam.agent.common.persistence.DomainTable;
import com.htam.agent.common.config.SerializableEnable;
import com.htam.agent.common.consts.TableConst;
import com.htam.agent.common.enums.RagDocumentStatus;
import lombok.*;

import java.time.LocalDateTime;

/**
 * RAG文档表
 *
 * @author huxuehao
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DomainTable(TableConst.RAG_DOCUMENT)
public class RagDocument implements SerializableEnable {

    @DomainId(type = DomainIdType.ASSIGN_ID)
    private Long id;

    private Long knowledgeBaseConfigId;

    private String fileName;

    private String filePath;

    private Long fileSize;

    private String fileType;

    private Integer chunkCount;

    private RagDocumentStatus status;

    private String errorMessage;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Long createdBy;

    private Long updatedBy;
}
