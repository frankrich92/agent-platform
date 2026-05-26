package com.htam.agent.runtime.agentscope.skill;

import com.htam.agent.runtime.agentscope.agui.AgentContext;
import com.htam.agent.runtime.agentscope.attachment.UploadedAttachmentRef;
import com.htam.agent.runtime.agentscope.attachment.UploadedAttachmentReferences;
import io.agentscope.core.skill.AgentSkill;
import java.util.List;

public final class UploadedAttachmentSkill {

    private static final String SKILL_NAME = "uploaded_attachment_context";

    private UploadedAttachmentSkill() {
    }

    public static boolean hasUploadedAttachments() {
        return AgentContext.getIfExists()
                .map(context -> !UploadedAttachmentReferences.from(context, null).isEmpty())
                .orElse(false);
    }

    public static AgentSkill getAgentSkill() {
        List<UploadedAttachmentRef> refs = AgentContext.getIfExists()
                .map(context -> UploadedAttachmentReferences.from(context, null))
                .orElse(List.of());
        return AgentSkill.builder()
                .name(SKILL_NAME)
                .description("Load this skill when the current turn contains uploaded attachments and file content is needed.")
                .skillContent(buildSkillContent(refs))
                .build();
    }

    private static String buildSkillContent(List<UploadedAttachmentRef> refs) {
        String attachmentPrompt = UploadedAttachmentReferences.toPrompt(refs);
        return """
            # Uploaded Attachment Context

            The current user turn contains uploaded attachments.

            ## Rules

            - Treat attachment IDs as opaque server-side references.
            - Do not assume file content from file names or extensions.
            - When the answer depends on file content, use an available file/attachment parsing skill or tool with the attachment_id.
            - If no parsing skill or tool is available, explain that the attachment reference is available but content cannot be inspected in this run.

            ## Current Attachments

            %s
            """.formatted(attachmentPrompt == null || attachmentPrompt.isBlank() ? "No uploaded attachments." : attachmentPrompt);
    }
}
