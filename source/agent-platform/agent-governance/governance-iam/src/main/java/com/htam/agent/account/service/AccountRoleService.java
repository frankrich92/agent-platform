package com.htam.agent.account.service;

import com.htam.agent.common.entity.AccountRole;

import java.util.List;

/**
 * 描述：账号角色服务
 *
 * @author huxuehao
 **/
public interface AccountRoleService {
    List<AccountRole> list();

    List<AccountRole> listByAccountId(Long accountId);

    boolean save(AccountRole accountRole);

    boolean saveBatch(List<AccountRole> accountRoles);

    boolean deleteByAccountId(Long accountId);

    boolean deleteByAccountIds(List<Long> accountIds);
}
