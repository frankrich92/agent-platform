package com.htam.agent.worker.file.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.htam.agent.common.entity.StorageProtocol;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.worker.file.storage.core.FileStorageService;

import java.util.List;

/**
 * 描述：文件存储协议配置
 *
 * @author huxuehao
 **/
public interface StorageProtocolService {
    IPage<StorageProtocol> page(PageParams pageParams, StorageProtocol query);

    StorageProtocol getById(Long id);

    boolean saveV2(StorageProtocol body);

    boolean updateV2(StorageProtocol body);

    boolean removeBatchByIds(List<Long> ids);

    /**
     * 根据ID设置启用
     * @param id id
     */
    boolean validSuccess(Long id);

    boolean updateProtocolConfig(Long id, String protocolConfig);

    /**
     * 获取存储服务
     */
    FileStorageService getStorageService();


}
