package com.htam.agent.account.service.impl;

import com.htam.agent.account.mapper.AccountRoleMapper;
import com.htam.agent.account.service.AccountRoleService;
import com.htam.agent.common.config.auth.AuthInterceptor;
import com.htam.agent.common.entity.AccountRole;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 账号ServiceRole实现
 *
 * @author huxuehao
 */
@Service
@RequiredArgsConstructor
public class AccountRoleServiceImpl extends ServiceImpl<AccountRoleMapper, AccountRole> implements AccountRoleService {

    @PostConstruct
    public void init() {
        for (AccountRole role : list()) {
            AuthInterceptor.setUserRole(role.getAccountId(), role.getRole());
        }
    }
}
