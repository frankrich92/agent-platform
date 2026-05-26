package com.htam.agent.profile.agent.service.impl;

import static com.htam.agent.profile.agent.service.impl.CapabilityPlanItemSupport.attributes;
import static com.htam.agent.profile.agent.service.impl.CapabilityPlanItemSupport.firstNonBlank;

import com.htam.agent.capability.CapabilityItem;
import com.htam.agent.capability.CapabilityKind;
import com.htam.agent.capability.CapabilityRiskLevel;
import com.htam.agent.capability.CapabilityRiskPolicy;
import com.htam.agent.common.entity.CodeExecutionConfig;
import com.htam.agent.worker.code.service.AgentCodeExecutionService;
import com.htam.agent.worker.code.service.CodeExecutionConfigService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(80)
@RequiredArgsConstructor
public class WorkerCapabilityContributor implements CapabilityPlanContributor {

    private final AgentCodeExecutionService agentCodeExecutionService;
    private final CodeExecutionConfigService codeExecutionConfigService;

    @Override
    public void contribute(CapabilityPlanContext context, List<CapabilityItem> items) {
        Long codeExecutionId = agentCodeExecutionService.getCodeExecutionIdByAgentId(context.agentId());
        if (codeExecutionId == null) {
            return;
        }
        CodeExecutionConfig config = codeExecutionConfigService.getById(codeExecutionId);
        if (config == null) {
            return;
        }
        boolean highRisk = Boolean.TRUE.equals(config.getEnableShell()) || Boolean.TRUE.equals(config.getEnableWrite());
        boolean readOnly = Boolean.TRUE.equals(config.getEnableRead()) && !highRisk;
        items.add(new CapabilityItem(
                CapabilityKind.WORKER,
                String.valueOf(config.getId()),
                firstNonBlank(config.getConfigName(), config.getId()),
                "worker:sandbox",
                Boolean.TRUE.equals(config.getEnabled()),
                readOnly,
                highRisk ? CapabilityRiskLevel.HIGH : CapabilityRiskLevel.LOW,
                highRisk ? CapabilityRiskPolicy.ASK : CapabilityRiskPolicy.ALLOW,
                null,
                List.of(),
                List.of(),
                attributes(
                        "workDir", config.getWorkDir(),
                        "uploadDir", config.getUploadDir(),
                        "autoUpload", config.getAutoUpload(),
                        "enableShell", config.getEnableShell(),
                        "enableRead", config.getEnableRead(),
                        "enableWrite", config.getEnableWrite(),
                        "allowedCommands", config.getCommand(),
                        "sandboxRequired", true)));
    }
}
