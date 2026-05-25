package com.htam.agent.common.entity;

import com.htam.agent.common.persistence.DomainIdType;
import com.htam.agent.common.persistence.DomainId;
import com.htam.agent.common.persistence.DomainTable;
import com.htam.agent.common.config.SerializableEnable;
import com.htam.agent.common.consts.TableConst;
import com.htam.agent.common.mp.annotation.QueryDefine;
import com.htam.agent.common.mp.support.QueryCondition;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 附件操作日志表
 *
 * @author huxuehao
 */
@Getter
@Setter
@DomainTable(TableConst.ATTACH_LOG)
public class AttachLog implements SerializableEnable {

    @DomainId(type = DomainIdType.ASSIGN_ID)
    private Long id;

    /**
     * 文件id
     */
    @QueryDefine(condition = QueryCondition.EQ)
    private Long fileId;

    /**
     * 附件原名
     */
    @QueryDefine(condition = QueryCondition.LIKE)
    private String originalName;

    /**
     * 附件拓展名
     */
    @QueryDefine(condition = QueryCondition.LIKE)
    private String extension;

    /**
     * 附件大小
     */
    private Long attachSize;

    /**
     * 操作人
     */
    private Long optUser;

    /**
     * 操作人名称
     */
    private String optUserName;

    /**
     * 操作时间
     */
    private LocalDateTime optTime;

    /**
     * 操作IP
     */
    private String optIp;

    /**
     * 操作类型
     */
    @QueryDefine(condition = QueryCondition.EQ)
    private String optType;
}
