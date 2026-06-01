package com.htam.agent.runtime.agentscope;

import com.htam.agent.common.util.AgentMetadataStore;
import io.agentscope.spring.boot.agui.common.ThreadSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 描述：定期清除代理元数据，避免内存溢出
 *
 * @author huxuehao
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class ClearAgentMetadataStore {
    private final ThreadSessionManager sessionManager;

    /**
     * 每天凌晨3点执行，清理无关联会话的 Agent 元数据
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void executeAt3AM() {
        log.info("开始清理过期 Agent 元数据，当前存储数量: {}", AgentMetadataStore.size());
        int removedCount = AgentMetadataStore.removeIf(this::isExpiredMetadata);
        log.info("清理完成，移除 {} 条记录，剩余: {}", removedCount, AgentMetadataStore.size());
    }

    private boolean isExpiredMetadata(Object agent, Map<String, Object> meta) {
        Object threadIdObj = meta.get("threadId");
        if (threadIdObj == null) {
            return true;
        }

        String threadId = threadIdObj.toString();
        try {
            return sessionManager.getSession(threadId).isEmpty();
        } catch (Exception e) {
            log.warn("检查会话 {} 时出错，将清理对应元数据", threadId, e);
            return true;
        }
    }
}
