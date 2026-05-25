package com.htam.agent.common.entity;

import com.htam.agent.common.persistence.DomainTable;
import com.htam.agent.common.consts.TableConst;
import lombok.Getter;
import lombok.Setter;

/**
 * 账号表
 *
 * @author huxuehao
 */
@Getter
@Setter
@DomainTable(value = TableConst.ACCOUNT)
public class Account extends BaseEntity {

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;
}
