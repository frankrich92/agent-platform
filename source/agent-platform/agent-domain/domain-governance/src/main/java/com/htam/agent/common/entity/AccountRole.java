package com.htam.agent.common.entity;

import com.htam.agent.common.config.SerializableEnable;
import com.htam.agent.common.consts.TableConst;
import com.htam.agent.common.enums.Role;
import com.htam.agent.common.persistence.DomainIdType;
import com.htam.agent.common.persistence.DomainId;
import com.htam.agent.common.persistence.DomainTable;
import lombok.*;

/**
 * 账号表
 *
 * @author huxuehao
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DomainTable(value = TableConst.ROLE, autoResultMap = true)
public class AccountRole implements SerializableEnable {
    @DomainId(type = DomainIdType.ASSIGN_ID)
    private Long id;
    private Long accountId;
    private Role role;
}
