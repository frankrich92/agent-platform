package com.htam.agent.repo.mybatis.capability;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.htam.agent.common.entity.McpServer;
import com.htam.agent.common.enums.McpActivationStatus;
import com.htam.agent.common.enums.McpProtocol;
import com.htam.agent.common.mp.support.MP;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.repo.mybatis.capability.mapper.McpServerMapper;
import com.htam.agent.repo.capability.McpServerRepository;
import com.htam.agent.repo.support.RepoPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class McpServerMybatisRepository implements McpServerRepository {
    private final McpServerMapper mcpServerMapper;

    @Override
    public RepoPage<McpServer> page(PageParams pageParams, String name, McpProtocol protocol, Boolean enabled) {
        IPage<McpServer> page = mcpServerMapper.selectPage(MP.getPage(pageParams),
                Wrappers.<McpServer>lambdaQuery()
                        .like(name != null && !name.isBlank(), McpServer::getName, name)
                        .eq(protocol != null, McpServer::getProtocol, protocol)
                        .eq(enabled != null, McpServer::getEnabled, enabled));
        return new RepoPage<>(page.getRecords(), page.getTotal(), page.getSize(), page.getCurrent());
    }

    @Override
    public McpServer getById(Long id) {
        return mcpServerMapper.selectById(id);
    }

    @Override
    public boolean save(McpServer entity) {
        return mcpServerMapper.insert(entity) > 0;
    }

    @Override
    public boolean updateById(McpServer entity) {
        return mcpServerMapper.updateById(entity) >= 0;
    }

    @Override
    public boolean deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }
        return mcpServerMapper.deleteBatchIds(ids) >= 0;
    }

    @Override
    public boolean beginActivation(Long id, String currentActivationRequestId, McpServer update) {
        LambdaUpdateWrapper<McpServer> wrapper = new LambdaUpdateWrapper<McpServer>()
                .eq(McpServer::getId, id);
        if (currentActivationRequestId == null) {
            wrapper.isNull(McpServer::getActivationRequestId);
        } else {
            wrapper.eq(McpServer::getActivationRequestId, currentActivationRequestId);
        }
        return mcpServerMapper.update(update, wrapper) > 0;
    }

    @Override
    public boolean finishActivation(Long id, String requestId, String configHash, McpServer update) {
        return mcpServerMapper.update(update, new LambdaUpdateWrapper<McpServer>()
                .eq(McpServer::getId, id)
                .eq(McpServer::getActivationRequestId, requestId)
                .eq(McpServer::getConfigHash, configHash)) > 0;
    }

    @Override
    public boolean autoDegrade(Long id, Long activationRevision, String configHash, McpServer update) {
        return mcpServerMapper.update(update, new LambdaUpdateWrapper<McpServer>()
                .eq(McpServer::getId, id)
                .eq(McpServer::getEnabled, true)
                .eq(McpServer::getActivationStatus, McpActivationStatus.ACTIVE)
                .eq(McpServer::getActivationRevision, activationRevision)
                .eq(McpServer::getConfigHash, configHash)) > 0;
    }
}
