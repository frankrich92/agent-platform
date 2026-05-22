package com.htam.agent.repo.iam;

import com.htam.agent.common.entity.Account;

import java.util.List;

public interface AccountRepository {
    List<Account> list(String nickname, String email, String username, Boolean enabled);

    Account getById(Long id);

    Account getById(String id);

    List<Account> listByIds(List<Long> ids);

    Account getByUsernameOrEmail(String usernameOrEmail);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByEmailExcludeId(String email, Long id);

    boolean save(Account account);

    boolean updateById(Account account);

    boolean deleteByIds(List<Long> ids);
}
