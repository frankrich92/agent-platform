package com.htam.agent.repo.iam;

import com.htam.agent.common.entity.SecretKey;

import java.util.List;

public interface SecretKeyRepository {
    List<SecretKey> list();

    boolean save(SecretKey secretKey);

    boolean updateById(SecretKey secretKey);

    boolean updateName(Long id, String name);

    boolean deleteByIds(List<Long> ids);
}
