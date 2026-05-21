package com.htam.agent.common.entity;

import com.htam.agent.common.config.SerializableEnable;
import com.htam.agent.common.consts.TableConst;
import com.htam.agent.common.enums.Role;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName(value = TableConst.ROLE, autoResultMap = true)
public class AccountRole implements SerializableEnable {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long accountId;
    private Role role;
}
