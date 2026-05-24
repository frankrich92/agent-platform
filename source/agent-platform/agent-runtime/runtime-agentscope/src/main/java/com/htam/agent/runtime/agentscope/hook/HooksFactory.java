package com.htam.agent.runtime.agentscope.hook;

import com.htam.agent.common.entity.AgentDefinition;
import com.htam.agent.common.entity.HookConfig;
import com.htam.agent.common.enums.CodeLanguage;
import com.htam.agent.common.enums.HookType;
import com.htam.agent.runtime.agentscope.hook.dynamices.HookInstanceLoadFactory;
import com.htam.agent.runtime.agentscope.workspace.hook.WorkspaceValidateHook;
import com.htam.agent.runtime.agentscope.workspace.hook.WorkspaceWebsocketHook;
import com.htam.agent.capability.tool.hook.service.AgentHookService;
import com.htam.agent.capability.tool.hook.service.HookConfigService;
import io.agentscope.core.hook.Hook;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述：钩子工厂
 *
 * @author huxuehao
 **/
@Component
@RequiredArgsConstructor
public class HooksFactory {
    private final AgentHookService agentHookService;
    private final HookConfigService hookConfigService;
    private final WorkspaceWebsocketHook workspaceWebsocketHook;

    public List<Hook> getHooks(AgentDefinition agentDefinition) {
        List<Hook> hooks = new ArrayList<>();

        // 配置工作空间专属Hook
        hooks.add(new WorkspaceValidateHook());
        hooks.add(workspaceWebsocketHook);

        List<Long> hookIds = agentHookService.getHookIds(agentDefinition.getId());
        if (hookIds.isEmpty()) {
            return hooks;
        }

        hookConfigService.listByIds(hookIds)
                .stream()
                .filter(HookConfig::getEnabled)
                .forEach(hookConfig -> {
                    Hook hook;
                    if (hookConfig.getHookType() == HookType.BUILTIN) {
                        hook = HooksRegister.getHook(hookConfig.getClassPath());
                    } else {
                        hook = HookInstanceLoadFactory.getInstanceLoader(CodeLanguage.JAVA)
                                .loadInstance(hookConfig.getCode());
                    }
                    hooks.add(hook);
                });

        return hooks;
    }
}
