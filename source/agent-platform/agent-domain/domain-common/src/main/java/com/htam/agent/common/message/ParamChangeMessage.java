package com.htam.agent.common.message;

import com.htam.agent.common.config.SerializableEnable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统参数变更 Redis 广播消息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParamChangeMessage implements SerializableEnable {
    private String paramKey;
    private String sourceNodeId;

    public static ParamChangeMessage create(String nodeId, String paramKey) {
        return ParamChangeMessage.builder()
                .sourceNodeId(nodeId)
                .paramKey(paramKey)
                .build();
    }
}
