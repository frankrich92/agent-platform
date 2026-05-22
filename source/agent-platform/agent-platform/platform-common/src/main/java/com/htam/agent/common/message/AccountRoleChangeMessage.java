package com.htam.agent.common.message;

import com.htam.agent.common.config.SerializableEnable;
import com.htam.agent.common.enums.Role;
import lombok.*;

/**
 * 描述：账号角色变化消息
 *
 * @author huxuehao
 **/
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountRoleChangeMessage implements SerializableEnable {
    private String accountId;
    private Role role;
}
