package com.htam.agent.resource.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htam.agent.common.entity.StorageProtocol;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.common.util.FuncUtils;
import com.htam.agent.repo.file.StorageProtocolRepository;
import com.htam.agent.repo.support.RepoPage;
import com.htam.agent.resource.storage.core.FileStorageService;
import com.htam.agent.resource.enums.ProtocolType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 描述：文件存储协议配置
 *
 * @author huxuehao
 **/
@Service
@RequiredArgsConstructor
public class StorageProtocolServiceImpl implements StorageProtocolService {
    private final StorageProtocolRepository storageProtocolRepository;

    @Override
    public IPage<StorageProtocol> page(PageParams pageParams, StorageProtocol query) {
        StorageProtocol storageProtocol = query == null ? new StorageProtocol() : query;
        RepoPage<StorageProtocol> repoPage = storageProtocolRepository.page(
                pageParams,
                storageProtocol.getName(),
                storageProtocol.getProtocol(),
                storageProtocol.getValid());
        IPage<StorageProtocol> page = new Page<>(repoPage.current(), repoPage.size(), repoPage.total());
        page.setRecords(repoPage.records());
        return page;
    }

    @Override
    public StorageProtocol getById(Long id) {
        return storageProtocolRepository.getById(id);
    }

    @Override
    public boolean saveV2(StorageProtocol body) {
        if(body.getValid() == 1) {
            List<StorageProtocol> validList = storageProtocolRepository.listValidExcludeId(body.getId());
            if(!FuncUtils.isEmpty(validList)) {
                body.setValid(0);
            }
        }
        return storageProtocolRepository.save(body);
    }

    @Override
    public boolean updateV2(StorageProtocol body) {
        if(body.getValid() == 1) {
            List<StorageProtocol> validList = storageProtocolRepository.listValidExcludeId(body.getId());
            if(!FuncUtils.isEmpty(validList)) {
                throw new RuntimeException("已存在有效的协议配置");
            }
        }

        StorageProtocol oldData = getById(body.getId());
        boolean clearProtocolConfig = !oldData.getProtocol().equals(body.getProtocol());
        return storageProtocolRepository.updateMainFields(body, clearProtocolConfig);
    }

    @Override
    public boolean removeBatchByIds(List<Long> ids) {
        return storageProtocolRepository.deleteByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean validSuccess(Long id) {
        StorageProtocol protocol = getById(id);
        if (protocol.getValid() == 1) {
            return true;
        }
        storageProtocolRepository.setAllInvalid();
        return storageProtocolRepository.setValid(id);
    }

    @Override
    public boolean updateProtocolConfig(Long id, String protocolConfig) {
        return storageProtocolRepository.updateProtocolConfig(id, protocolConfig);
    }

    @Override
    public FileStorageService getStorageService() {
        StorageProtocol storageProtocol = getCurrentValidProtocol();
        return createStorageService(storageProtocol);
    }

    /**
     * 获取当前有效的协议
     */
    private StorageProtocol getCurrentValidProtocol() {
        List<StorageProtocol> list = storageProtocolRepository.listCurrentValid();
        if (FuncUtils.isEmpty(list) || list.size() > 1) {
            throw new RuntimeException("存储配置不存咋唯一一个有效的配置");
        }
        return list.getFirst();
    }

    /**
     * 创建存储服务
     * @param storageProtocol 存储协议实体
     */
    private FileStorageService createStorageService(StorageProtocol storageProtocol) {
        String protocolConfig = storageProtocol.getProtocolConfig();
        if (FuncUtils.isEmpty(protocolConfig)) {
            throw new RuntimeException("请完善协议配置");
        }

        return FileStorageService.create(ProtocolType.valueOf(storageProtocol.getProtocol()), protocolConfig);
    }
}
