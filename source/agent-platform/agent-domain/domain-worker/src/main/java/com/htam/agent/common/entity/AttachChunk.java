package com.htam.agent.common.entity;

import com.htam.agent.common.persistence.DomainIdType;
import com.htam.agent.common.persistence.DomainId;
import com.htam.agent.common.persistence.DomainTable;
import com.htam.agent.common.config.SerializableEnable;
import com.htam.agent.common.consts.TableConst;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 附件分片记录表
 *
 * @author huxuehao
 */
@Getter
@Setter
@DomainTable(TableConst.ATTACH_CHUNK)
public class AttachChunk implements SerializableEnable {

    @DomainId(type = DomainIdType.ASSIGN_ID)
    private Long id;

    /**
     * 分片的hash值
     */
    private String chunkHash;

    /**
     * 分片的索引
     */
    private Integer chunkIndex;

    /**
     * 分片总数
     */
    private Integer chunkTotals;

    /**
     * 文件唯一标识
     */
    private String fileKey;

    /**
     * 文件大小
     */
    private Integer fileTotalSize;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 创建人
     */
    private Long createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createAt;
}
