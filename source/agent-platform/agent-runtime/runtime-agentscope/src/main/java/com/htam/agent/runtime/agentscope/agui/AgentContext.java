package com.htam.agent.runtime.agentscope.agui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.htam.agent.common.entity.AgentDefinition;
import com.htam.agent.common.util.JsonUtils;
import com.htam.agent.common.vo.AccountVO;
import io.agentscope.core.agui.model.RunAgentInput;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * 描述：智能体上下文
 *
 * @author huxuehao
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentContext {
    private static final InheritableThreadLocal<AgentContext> CONTEXT_HOLDER = new InheritableThreadLocal<>();

    private String threadId;
    private String runId;
    private boolean memoryActive;
    private boolean planActive;
    private List<String> fileIds;
    private List<Map<String, Object>> fileAttachments;
    private AccountVO userInfo;
    private AgentDefinition agentDefinition;
    private Map<String, Object> params;

    public static void init(RunAgentInput input, String threadId) {
        // 创建上下文
        AgentContext agentContext = AgentContext.get();

        agentContext.setThreadId(threadId);

        agentContext.setRunId(input.getRunId());

        boolean memoryActive = input.getForwardedProp("memoryActive") != null
                ? (Boolean) input.getForwardedProp("memoryActive")
                : false;
        agentContext.setMemoryActive(memoryActive);

        agentContext.setPlanActive(
                input.getForwardedProp("planActive") != null
                        ? (Boolean) input.getForwardedProp("planActive")
                        : false);

        List<Map<String, Object>> fileAttachments = toMapList(input.getForwardedProp("fileAttachments"));
        agentContext.setFileAttachments(fileAttachments);

        List<String> fileIds = toList(input.getForwardedProp("fileIds"));
        if (fileIds.isEmpty() && !fileAttachments.isEmpty()) {
            fileIds = fileAttachments.stream()
                    .map(item -> item.get("id"))
                    .filter(Objects::nonNull)
                    .map(String::valueOf)
                    .toList();
        }
        agentContext.setFileIds(fileIds);

        AccountVO userInfo = input.getForwardedProp("userInfo") != null
                ? JsonUtils.objectToBean(input.getForwardedProp("userInfo"), AccountVO.class)
                : null;
        agentContext.setUserInfo(userInfo);

        agentContext.setParams(toMap(input.getForwardedProp("params")));
    }

    private static Map<String, Object> toMap(Object params) {
        if (params == null) {
            return new HashMap<>();
        }

        return JsonUtils.parse(JsonUtils.toJsonStr(params), new TypeReference<Map<String, Object>>() {});
    }

    private static List<String> toList(Object params) {
        if (params == null) {
            return new ArrayList<>();
        }
        return JsonUtils.parse(JsonUtils.toJsonStr(params), new TypeReference<List<String>>() {});
    }

    private static List<Map<String, Object>> toMapList(Object params) {
        if (params == null) {
            return new ArrayList<>();
        }
        return JsonUtils.parse(JsonUtils.toJsonStr(params), new TypeReference<List<Map<String, Object>>>() {});
    }

    public static AgentContext get() {
        AgentContext agentContext = CONTEXT_HOLDER.get();
        if (agentContext == null) {
            agentContext = new AgentContext();
            CONTEXT_HOLDER.set(agentContext);
        }

        return CONTEXT_HOLDER.get();
    }

    public static Optional<AgentContext> getIfExists() {
        return Optional.ofNullable(CONTEXT_HOLDER.get());
    }

    public static void set(AgentContext agentContext) {
        CONTEXT_HOLDER.set(agentContext);
    }

    public static boolean exist() {
        return CONTEXT_HOLDER.get() != null;
    }

    public static void clean() {
        CONTEXT_HOLDER.remove();
    }
}
