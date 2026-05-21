package com.htam.agent.repo.file;

import com.htam.agent.common.entity.StorageProtocol;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.repo.support.RepoPage;

import java.util.List;

public interface StorageProtocolRepository {
    RepoPage<StorageProtocol> page(PageParams pageParams, String name, String protocol, Integer valid);

    StorageProtocol getById(Long id);

    List<StorageProtocol> listValidExcludeId(Long id);

    List<StorageProtocol> listCurrentValid();

    boolean save(StorageProtocol body);

    boolean updateMainFields(StorageProtocol body, boolean clearProtocolConfig);

    boolean setAllInvalid();

    boolean setValid(Long id);

    boolean updateProtocolConfig(Long id, String protocolConfig);

    boolean deleteByIds(List<Long> ids);
}
