package com.htam.agent.repo.mybatis.iam;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.htam.agent.account.mapper.AccountRoleMapper;
import com.htam.agent.common.entity.AccountRole;
import com.htam.agent.repo.iam.AccountRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AccountRoleMybatisRepository implements AccountRoleRepository {
    private final AccountRoleMapper accountRoleMapper;

    @Override
    public List<AccountRole> list() {
        return accountRoleMapper.selectList(Wrappers.emptyWrapper());
    }

    @Override
    public List<AccountRole> listByAccountId(Long accountId) {
        return accountRoleMapper.selectList(Wrappers.<AccountRole>lambdaQuery()
                .eq(AccountRole::getAccountId, accountId));
    }

    @Override
    public boolean save(AccountRole accountRole) {
        return accountRoleMapper.insert(accountRole) > 0;
    }

    @Override
    public boolean saveBatch(List<AccountRole> accountRoles) {
        if (accountRoles == null || accountRoles.isEmpty()) {
            return true;
        }
        accountRoles.forEach(accountRoleMapper::insert);
        return true;
    }

    @Override
    public boolean deleteByAccountId(Long accountId) {
        return accountRoleMapper.delete(Wrappers.<AccountRole>lambdaQuery()
                .eq(AccountRole::getAccountId, accountId)) >= 0;
    }

    @Override
    public boolean deleteByAccountIds(List<Long> accountIds) {
        if (accountIds == null || accountIds.isEmpty()) {
            return true;
        }
        return accountRoleMapper.delete(Wrappers.<AccountRole>lambdaQuery()
                .in(AccountRole::getAccountId, accountIds)) >= 0;
    }
}
