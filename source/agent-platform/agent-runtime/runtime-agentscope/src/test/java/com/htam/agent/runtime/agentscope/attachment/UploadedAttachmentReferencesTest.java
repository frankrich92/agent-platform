package com.htam.agent.runtime.agentscope.attachment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.htam.agent.runtime.agentscope.agui.AgentContext;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class UploadedAttachmentReferencesTest {

    @Test
    void buildsAttachmentReferencesFromMessagePrefixAndContext() {
        AgentContext context = new AgentContext();
        context.setFileIds(List.of("42"));
        context.setFileAttachments(List.of(Map.of(
                "id", "42",
                "name", "budget.xlsx",
                "extension", "xlsx",
                "size", "12 KB")));

        String content = """
                {"files":[{"id":"42","name":"budget.xlsx","extension":"xlsx","size":"12 KB"}]}@==##::::##==@请总结这个文件
                """.trim();

        List<UploadedAttachmentRef> refs = UploadedAttachmentReferences.from(context, content);

        assertEquals("请总结这个文件", UploadedAttachmentReferences.stripFilePrefix(content));
        assertEquals(1, refs.size());
        assertEquals("42", refs.getFirst().id());
        assertEquals("budget.xlsx", refs.getFirst().name());
        assertTrue(UploadedAttachmentReferences.toPrompt(refs).contains("attachment_id: 42"));
    }
}
