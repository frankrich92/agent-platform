package com.htam.agent.repo.capability;

import com.htam.agent.common.entity.McpServer;
import com.htam.agent.common.enums.McpProtocol;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.repo.support.RepoPage;

import java.util.List;

public interface McpServerRepository {
    RepoPage<McpServer> page(PageParams pageParams, String name, McpProtocol protocol, Boolean enabled);

    McpServer getById(Long id);

    boolean save(McpServer entity);

    boolean updateById(McpServer entity);

    boolean deleteByIds(List<Long> ids);

    boolean beginActivation(Long id, String currentActivationRequestId, McpServer update);

    boolean finishActivation(Long id, String requestId, String configHash, McpServer update);

    boolean autoDegrade(Long id, Long activationRevision, String configHash, McpServer update);
}
