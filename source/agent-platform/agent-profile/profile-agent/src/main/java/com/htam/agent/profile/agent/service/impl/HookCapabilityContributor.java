package com.htam.agent.profile.agent.service.impl;

import static com.htam.agent.profile.agent.service.impl.CapabilityPlanItemSupport.attributes;
import static com.htam.agent.profile.agent.service.impl.CapabilityPlanItemSupport.firstNonBlank;
import static com.htam.agent.profile.agent.service.impl.CapabilityPlanItemSupport.namespace;

import com.htam.agent.capability.CapabilityItem;
import com.htam.agent.capability.CapabilityKind;
import com.htam.agent.capability.CapabilityRiskLevel;
import com.htam.agent.capability.CapabilityRiskPolicy;
import com.htam.agent.capability.tool.HookPolicy;
import com.htam.agent.capability.tool.hook.service.AgentHookService;
import com.htam.agent.capability.tool.hook.service.HookConfigService;
import com.htam.agent.common.entity.HookConfig;
import com.htam.agent.common.enums.HookType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(CapabilityPlanContributor.ORDER_HOOK)
@RequiredArgsConstructor
public class HookCapabilityContributor implements CapabilityPlanContributor {

    private final AgentHookService agentHookService;
    private final HookConfigService hookConfigService;

    @Override
    public void contribute(CapabilityPlanContext context, List<CapabilityItem> items) {
        HookPolicy lowRiskPolicy = HookPolicy.lowRiskDefault();
        for (Long hookId : agentHookService.getHookIds(context.agentId())) {
            HookConfig hook = hookConfigService.getById(hookId);
            if (hook == null) {
                continue;
            }

            boolean builtin = hook.getHookType() == HookType.BUILTIN;
            items.add(new CapabilityItem(
                    CapabilityKind.HOOK,
                    String.valueOf(hook.getId()),
                    firstNonBlank(hook.getName(), hook.getId()),
                    namespace("hook", hook.getHookType() == null ? null : hook.getHookType().name().toLowerCase()),
                    Boolean.TRUE.equals(hook.getEnabled()) && builtin,
                    false,
                    builtin ? CapabilityRiskLevel.LOW : CapabilityRiskLevel.HIGH,
                    builtin ? CapabilityRiskPolicy.ALLOW : CapabilityRiskPolicy.DENY,
                    null,
                    List.of(),
                    List.of(),
                    attributes(
                            "hookType", hook.getHookType(),
                            "description", hook.getDescription(),
                            "classPath", builtin ? hook.getClassPath() : null,
                            "priority", hook.getPriority(),
                            "deterministicOnly", lowRiskPolicy.deterministicOnly(),
                            "allowedLifecyclePhases", lowRiskPolicy.phases().stream().map(Enum::name).toList(),
                            "maxRiskLevel", lowRiskPolicy.maxRiskLevel())));
        }
    }
}
