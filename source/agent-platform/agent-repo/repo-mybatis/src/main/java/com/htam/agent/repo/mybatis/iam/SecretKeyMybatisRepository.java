package com.htam.agent.repo.mybatis.iam;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.htam.agent.common.entity.SecretKey;
import com.htam.agent.repo.iam.SecretKeyRepository;
import com.htam.agent.sk.mapper.SecretKeyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SecretKeyMybatisRepository implements SecretKeyRepository {
    private final SecretKeyMapper secretKeyMapper;

    @Override
    public List<SecretKey> list() {
        return secretKeyMapper.selectList(Wrappers.emptyWrapper());
    }

    @Override
    public boolean save(SecretKey secretKey) {
        return secretKeyMapper.insert(secretKey) > 0;
    }

    @Override
    public boolean updateById(SecretKey secretKey) {
        return secretKeyMapper.updateById(secretKey) > 0;
    }

    @Override
    public boolean updateName(Long id, String name) {
        return secretKeyMapper.update(null, Wrappers.<SecretKey>lambdaUpdate()
                .eq(SecretKey::getId, id)
                .set(SecretKey::getName, name)) > 0;
    }

    @Override
    public boolean deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        return secretKeyMapper.deleteBatchIds(ids) > 0;
    }
}
