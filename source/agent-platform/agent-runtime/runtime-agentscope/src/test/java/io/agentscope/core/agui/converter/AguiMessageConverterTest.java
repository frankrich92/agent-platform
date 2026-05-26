package io.agentscope.core.agui.converter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.htam.agent.runtime.agentscope.agui.AgentContext;
import io.agentscope.core.agui.model.AguiMessage;
import io.agentscope.core.message.Msg;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AguiMessageConverterTest {

    @Test
    void appendsUploadedAttachmentReferencesWithoutParsingFileContent() {
        AgentContext context = new AgentContext();
        context.setFileIds(List.of("42"));
        context.setFileAttachments(List.of(Map.of(
                "id", "42",
                "name", "budget.xlsx",
                "extension", "xlsx",
                "size", "12 KB")));
        AgentContext.set(context);
        try {
            String content = """
                    {"files":[{"id":"42","name":"budget.xlsx","extension":"xlsx","size":"12 KB"}]}@==##::::##==@请总结这个文件
                    """.trim();

            List<Msg> messages = new AguiMessageConverter()
                    .toMsgList(List.of(AguiMessage.userMessage("m1", content)));

            String text = messages.getFirst().getTextContent();
            assertTrue(text.contains("请总结这个文件"));
            assertTrue(text.contains("attachment_id: 42"));
            assertTrue(text.contains("Use an available file/attachment parsing skill or tool"));
            assertFalse(text.contains("@==##::::##==@"));
            assertFalse(text.contains("\"files\""));
        } finally {
            AgentContext.clean();
        }
    }
}
