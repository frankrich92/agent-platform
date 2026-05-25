package com.htam.agent.common.entity;

import com.htam.agent.common.persistence.DomainIdType;
import com.htam.agent.common.persistence.DomainId;
import com.htam.agent.common.persistence.DomainTable;
import com.htam.agent.common.config.SerializableEnable;
import com.htam.agent.common.consts.TableConst;
import lombok.*;

import java.time.LocalDateTime;

/**
 * RAG文档分块表
 *
 * @author huxuehao
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DomainTable(TableConst.RAG_DOCUMENT_CHUNK)
public class RagDocumentChunk implements SerializableEnable {

    @DomainId(type = DomainIdType.ASSIGN_ID)
    private Long id;

    private Long documentId;

    private String fileName;

    private Integer chunkIndex;

    private String content;

    private Integer tokenCount;

    private Integer startOffset;

    private Integer endOffset;

    private String metadata;

    private LocalDateTime createdAt;
}
