package com.htam.agent.repo.iam;

import com.htam.agent.common.entity.AccountRole;

import java.util.List;

public interface AccountRoleRepository {
    List<AccountRole> list();

    List<AccountRole> listByAccountId(Long accountId);

    boolean save(AccountRole accountRole);

    boolean saveBatch(List<AccountRole> accountRoles);

    boolean deleteByAccountId(Long accountId);

    boolean deleteByAccountIds(List<Long> accountIds);
}
