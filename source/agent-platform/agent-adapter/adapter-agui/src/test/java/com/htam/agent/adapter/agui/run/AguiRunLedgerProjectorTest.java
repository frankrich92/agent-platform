package com.htam.agent.adapter.agui.run;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.htam.agent.common.dto.ChatMessageAppendDTO;
import com.htam.agent.common.vo.ChatMessageVO;
import com.htam.agent.governance.observability.TraceContextPlanner;
import com.htam.agent.run.session.lock.SessionLockManager;
import com.htam.agent.run.session.service.ChatSessionService;
import io.agentscope.core.agui.event.AguiEvent;
import io.agentscope.core.agui.model.RunAgentInput;
import io.agentscope.core.agui.observer.AguiRunEventContext;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class AguiRunLedgerProjectorTest {

    @Test
    void appendsAssistantMessageWithCapturedRequestUserId() {
        AtomicReference<Long> capturedSessionId = new AtomicReference<>();
        AtomicReference<Long> capturedUserId = new AtomicReference<>();
        AtomicReference<ChatMessageAppendDTO> capturedMessage = new AtomicReference<>();
        ChatSessionService chatSessionService = (ChatSessionService) Proxy.newProxyInstance(
                ChatSessionService.class.getClassLoader(),
                new Class<?>[] {ChatSessionService.class},
                (proxy, method, args) -> {
                    if ("appendMessageAsUser".equals(method.getName())) {
                        capturedSessionId.set((Long) args[0]);
                        capturedUserId.set((Long) args[1]);
                        capturedMessage.set((ChatMessageAppendDTO) args[2]);
                        return new ChatMessageVO();
                    }
                    throw new UnsupportedOperationException(method.getName());
                });

        AguiRunLedgerProjector projector = new AguiRunLedgerProjector(
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                chatSessionService,
                new SessionLockManager(),
                null,
                null,
                new TraceContextPlanner());
        RunAgentInput input = new RunAgentInput(
                "2059118461698736129",
                "run-test",
                List.of(),
                List.of(),
                List.of(),
                Map.of(),
                Map.of());
        AguiRunEventContext context = new AguiRunEventContext(input, "agent-code", 1111111111111111111L);

        projector.onEvent(context, new AguiEvent.TextMessageContent(
                "2059118461698736129",
                "run-test",
                "msg-1",
                "完成"));
        projector.onEvent(context, new AguiEvent.TextMessageEnd(
                "2059118461698736129",
                "run-test",
                "msg-1"));

        assertEquals(2059118461698736129L, capturedSessionId.get());
        assertEquals(1111111111111111111L, capturedUserId.get());
        assertEquals("assistant", capturedMessage.get().getRole());
        assertTrue(capturedMessage.get().getContent().contains("完成"));
    }
}
