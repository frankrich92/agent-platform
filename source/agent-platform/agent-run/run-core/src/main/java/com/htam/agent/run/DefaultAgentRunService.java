package com.htam.agent.run;

import com.htam.agent.profile.agent.service.ChatSessionService;
import com.htam.agent.capability.CapabilityPlan;
import com.htam.agent.capability.CapabilityPlanService;
import com.htam.agent.common.dto.ChatMessageAppendDTO;
import com.htam.agent.common.vo.ChatMessageVO;
import com.htam.agent.runtime.AgentRunRequest;
import com.htam.agent.runtime.AgentRunResult;
import com.htam.agent.runtime.AgentRuntimeRunner;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DefaultAgentRunService implements AgentRunService {

    private final AgentRuntimeRunner runtimeRunner;
    private final CapabilityPlanService capabilityPlanService;
    private final ChatSessionService chatSessionService;

    public DefaultAgentRunService(
            AgentRuntimeRunner runtimeRunner,
            CapabilityPlanService capabilityPlanService,
            ChatSessionService chatSessionService) {
        this.runtimeRunner = runtimeRunner;
        this.capabilityPlanService = capabilityPlanService;
        this.chatSessionService = chatSessionService;
    }

    @Override
    public AgentRunSummary run(AgentRunCommand command) {
        String runId = command.runId() == null || command.runId().isBlank()
                ? UUID.randomUUID().toString()
                : command.runId();
        CapabilityPlan capabilityPlan = capabilityPlanService.resolvePlan(command.agentId());

        ChatMessageVO userMessage = null;
        if (command.recordMessages() && command.sessionId() != null) {
            userMessage = appendMessage(command.sessionId(), "user", command.input());
        }

        AgentRunResult runtimeResult = runtimeRunner.run(new AgentRunRequest(
                command.agentId(),
                command.input(),
                command.sessionId() == null ? null : String.valueOf(command.sessionId()),
                runId,
                capabilityPlan.planId(),
                null));

        ChatMessageVO assistantMessage = null;
        if (command.recordMessages() && command.sessionId() != null && runtimeResult.message() != null) {
            assistantMessage = appendMessage(command.sessionId(), "assistant", runtimeResult.message());
        }

        return new AgentRunSummary(
                runId,
                command.agentId(),
                command.sessionId(),
                runtimeResult,
                capabilityPlan,
                userMessage == null ? null : userMessage.getId(),
                assistantMessage == null ? null : assistantMessage.getId());
    }

    private ChatMessageVO appendMessage(Long sessionId, String role, String content) {
        ChatMessageAppendDTO dto = new ChatMessageAppendDTO();
        dto.setRole(role);
        dto.setContent(content);
        return chatSessionService.appendMessage(sessionId, dto);
    }
}
