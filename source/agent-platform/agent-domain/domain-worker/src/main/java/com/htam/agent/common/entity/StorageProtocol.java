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
 * 文件存储协议配置
 *
 * @author huxuehao
 */
@Getter
@Setter
@DomainTable(TableConst.STORAGE)
public class StorageProtocol implements SerializableEnable {

    @DomainId(type = DomainIdType.ASSIGN_ID)
    private Long id;

    /**
     * 名称
     */
    @QueryDefine(condition = QueryCondition.LIKE)
    private String name;

    /**
     * 存储协议
     */
    @QueryDefine(condition = QueryCondition.EQ)
    private String protocol;

    /**
     * 协议配置
     */
    private String protocolConfig;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createAt;

    /**
     * 修改人
     */
    private String updateBy;

    /**
     * 修改时间
     */
    private LocalDateTime updateAt;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否有效
     */
    @QueryDefine(condition = QueryCondition.EQ)
    private Integer valid;
}
