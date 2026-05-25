package com.htam.agent.common.entity;

import com.htam.agent.common.persistence.DomainIdType;
import com.htam.agent.common.persistence.DomainId;
import com.htam.agent.common.persistence.DomainTable;
import com.htam.agent.common.config.SerializableEnable;
import com.htam.agent.common.consts.TableConst;
import com.htam.agent.common.mp.annotation.QueryDefine;
import com.htam.agent.common.mp.support.QueryCondition;
import lombok.Data;

/**
 * 描述：参数表
 *
 * @author huxuehao
 **/
@Data
@DomainTable(TableConst.PARAMS)
public class Params implements SerializableEnable {
    @DomainId(value = "id", type = DomainIdType.ASSIGN_ID)
    private Long id;

    @QueryDefine(condition = QueryCondition.LIKE)
    private String paramName;

    @QueryDefine(condition = QueryCondition.LIKE)
    private String paramKey;

    @QueryDefine(condition = QueryCondition.LIKE)
    private String paramValue;
}
