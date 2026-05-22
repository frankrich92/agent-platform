package com.htam.agent.repo.mybatis.file;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.htam.agent.common.entity.StorageProtocol;
import com.htam.agent.common.mp.support.MP;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.repo.file.StorageProtocolRepository;
import com.htam.agent.repo.support.RepoPage;
import com.htam.agent.resource.mapper.StorageProtocolMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class StorageProtocolMybatisRepository implements StorageProtocolRepository {
    private final StorageProtocolMapper storageProtocolMapper;

    @Override
    public RepoPage<StorageProtocol> page(PageParams pageParams, String name, String protocol, Integer valid) {
        IPage<StorageProtocol> page = storageProtocolMapper.selectPage(MP.getPage(pageParams),
                Wrappers.<StorageProtocol>lambdaQuery()
                        .like(name != null && !name.isBlank(), StorageProtocol::getName, name)
                        .eq(protocol != null && !protocol.isBlank(), StorageProtocol::getProtocol, protocol)
                        .eq(valid != null, StorageProtocol::getValid, valid));
        return new RepoPage<>(page.getRecords(), page.getTotal(), page.getSize(), page.getCurrent());
    }

    @Override
    public StorageProtocol getById(Long id) {
        return storageProtocolMapper.selectById(id);
    }

    @Override
    public List<StorageProtocol> listValidExcludeId(Long id) {
        return storageProtocolMapper.selectList(Wrappers.<StorageProtocol>lambdaQuery()
                .eq(StorageProtocol::getValid, 1)
                .ne(id != null, StorageProtocol::getId, id));
    }

    @Override
    public List<StorageProtocol> listCurrentValid() {
        return storageProtocolMapper.selectList(Wrappers.<StorageProtocol>lambdaQuery()
                .eq(StorageProtocol::getValid, 1));
    }

    @Override
    public boolean save(StorageProtocol body) {
        return storageProtocolMapper.insert(body) > 0;
    }

    @Override
    public boolean updateMainFields(StorageProtocol body, boolean clearProtocolConfig) {
        return storageProtocolMapper.update(null, Wrappers.<StorageProtocol>lambdaUpdate()
                .eq(StorageProtocol::getId, body.getId())
                .set(StorageProtocol::getName, body.getName())
                .set(StorageProtocol::getProtocol, body.getProtocol())
                .set(StorageProtocol::getValid, body.getValid())
                .set(StorageProtocol::getRemark, body.getRemark())
                .set(clearProtocolConfig, StorageProtocol::getProtocolConfig, null)) > 0;
    }

    @Override
    public boolean setAllInvalid() {
        return storageProtocolMapper.update(null, Wrappers.<StorageProtocol>lambdaUpdate()
                .set(StorageProtocol::getValid, 0)) >= 0;
    }

    @Override
    public boolean setValid(Long id) {
        return storageProtocolMapper.update(null, Wrappers.<StorageProtocol>lambdaUpdate()
                .eq(StorageProtocol::getId, id)
                .set(StorageProtocol::getValid, 1)) > 0;
    }

    @Override
    public boolean updateProtocolConfig(Long id, String protocolConfig) {
        return storageProtocolMapper.update(null, Wrappers.<StorageProtocol>lambdaUpdate()
                .eq(StorageProtocol::getId, id)
                .set(StorageProtocol::getProtocolConfig, protocolConfig)) > 0;
    }

    @Override
    public boolean deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        return storageProtocolMapper.deleteBatchIds(ids) > 0;
    }
}
