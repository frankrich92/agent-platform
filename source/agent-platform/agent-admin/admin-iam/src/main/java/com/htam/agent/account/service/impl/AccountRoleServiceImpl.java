package com.htam.agent.account.service.impl;

import com.htam.agent.account.service.AccountRoleService;
import com.htam.agent.common.config.auth.AuthInterceptor;
import com.htam.agent.common.entity.AccountRole;
import com.htam.agent.repo.iam.AccountRoleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 账号ServiceRole实现
 *
 * @author huxuehao
 */
@Service
@RequiredArgsConstructor
public class AccountRoleServiceImpl implements AccountRoleService {
    private final AccountRoleRepository accountRoleRepository;

    @Override
    public List<AccountRole> list() {
        return accountRoleRepository.list();
    }

    @Override
    public List<AccountRole> listByAccountId(Long accountId) {
        return accountRoleRepository.listByAccountId(accountId);
    }

    @Override
    public boolean save(AccountRole accountRole) {
        return accountRoleRepository.save(accountRole);
    }

    @Override
    public boolean saveBatch(List<AccountRole> accountRoles) {
        return accountRoleRepository.saveBatch(accountRoles);
    }

    @Override
    public boolean deleteByAccountId(Long accountId) {
        return accountRoleRepository.deleteByAccountId(accountId);
    }

    @Override
    public boolean deleteByAccountIds(List<Long> accountIds) {
        return accountRoleRepository.deleteByAccountIds(accountIds);
    }

    @PostConstruct
    public void init() {
        for (AccountRole role : list()) {
            AuthInterceptor.setUserRole(role.getAccountId(), role.getRole());
        }
    }
}
