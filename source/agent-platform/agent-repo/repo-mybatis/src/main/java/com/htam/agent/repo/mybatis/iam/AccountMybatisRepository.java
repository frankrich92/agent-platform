package com.htam.agent.repo.mybatis.iam;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.htam.agent.account.mapper.AccountMapper;
import com.htam.agent.common.entity.Account;
import com.htam.agent.repo.iam.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AccountMybatisRepository implements AccountRepository {
    private final AccountMapper accountMapper;

    @Override
    public List<Account> list(String nickname, String email, String username, Boolean enabled) {
        return accountMapper.selectList(Wrappers.<Account>lambdaQuery()
                .like(nickname != null && !nickname.isBlank(), Account::getNickname, nickname)
                .like(email != null && !email.isBlank(), Account::getEmail, email)
                .like(username != null && !username.isBlank(), Account::getUsername, username)
                .eq(enabled != null, Account::getEnabled, enabled));
    }

    @Override
    public Account getById(Long id) {
        return accountMapper.selectById(id);
    }

    @Override
    public Account getById(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return accountMapper.selectById(Long.valueOf(id));
    }

    @Override
    public List<Account> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return accountMapper.selectBatchIds(ids);
    }

    @Override
    public Account getByUsernameOrEmail(String usernameOrEmail) {
        return accountMapper.selectOne(Wrappers.<Account>lambdaQuery()
                .and(wrapper -> wrapper
                        .eq(Account::getUsername, usernameOrEmail)
                        .or()
                        .eq(Account::getEmail, usernameOrEmail)));
    }

    @Override
    public boolean existsByUsername(String username) {
        return accountMapper.exists(Wrappers.<Account>lambdaQuery()
                .eq(Account::getUsername, username));
    }

    @Override
    public boolean existsByEmail(String email) {
        return accountMapper.exists(Wrappers.<Account>lambdaQuery()
                .eq(Account::getEmail, email));
    }

    @Override
    public boolean existsByEmailExcludeId(String email, Long id) {
        return accountMapper.exists(Wrappers.<Account>lambdaQuery()
                .eq(Account::getEmail, email)
                .ne(Account::getId, id));
    }

    @Override
    public boolean save(Account account) {
        return accountMapper.insert(account) > 0;
    }

    @Override
    public boolean updateById(Account account) {
        return accountMapper.updateById(account) > 0;
    }

    @Override
    public boolean deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }
        return accountMapper.deleteBatchIds(ids) > 0;
    }
}
