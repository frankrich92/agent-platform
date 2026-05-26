package com.htam.agent.runtime.agentscope.attachment;

import com.fasterxml.jackson.databind.JsonNode;
import com.htam.agent.common.util.JsonUtils;
import com.htam.agent.runtime.agentscope.agui.AgentContext;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class UploadedAttachmentReferences {

    public static final String FILE_PREFIX_SEPARATOR = "@==##::::##==@";

    private UploadedAttachmentReferences() {
    }

    public static String stripFilePrefix(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        int splitIndex = content.indexOf(FILE_PREFIX_SEPARATOR);
        if (splitIndex < 0) {
            return content;
        }
        return content.substring(splitIndex + FILE_PREFIX_SEPARATOR.length());
    }

    public static List<UploadedAttachmentRef> from(AgentContext context, String messageContent) {
        LinkedHashMap<String, UploadedAttachmentRef> refs = new LinkedHashMap<>();
        for (UploadedAttachmentRef ref : fromMessagePrefix(messageContent)) {
            addOrMerge(refs, ref);
        }
        if (context != null) {
            if (context.getFileAttachments() != null) {
                for (Map<String, Object> attachment : context.getFileAttachments()) {
                    addOrMerge(refs, fromMap(attachment));
                }
            }
            if (context.getFileIds() != null) {
                for (String fileId : context.getFileIds()) {
                    addOrMerge(refs, new UploadedAttachmentRef(fileId, null, null, null));
                }
            }
        }
        return refs.values().stream()
                .filter(UploadedAttachmentRef::hasId)
                .toList();
    }

    public static String toPrompt(List<UploadedAttachmentRef> refs) {
        if (refs == null || refs.isEmpty()) {
            return "";
        }
        StringBuilder prompt = new StringBuilder();
        prompt.append("<uploaded_attachments>\n");
        prompt.append("The user uploaded attachments for this turn. The server has not parsed file contents. ");
        prompt.append("Use an available file/attachment parsing skill or tool with attachment_id when the task depends on file content. ");
        prompt.append("Do not infer file content from the file name alone.\n");
        for (int i = 0; i < refs.size(); i++) {
            UploadedAttachmentRef ref = refs.get(i);
            prompt.append("- index: ").append(i + 1).append('\n');
            prompt.append("  attachment_id: ").append(safe(ref.id())).append('\n');
            appendIfPresent(prompt, "  name: ", ref.name());
            appendIfPresent(prompt, "  extension: ", ref.extension());
            appendIfPresent(prompt, "  size: ", ref.size());
        }
        prompt.append("</uploaded_attachments>");
        return prompt.toString();
    }

    private static List<UploadedAttachmentRef> fromMessagePrefix(String content) {
        if (content == null || content.isEmpty()) {
            return List.of();
        }
        int splitIndex = content.indexOf(FILE_PREFIX_SEPARATOR);
        if (splitIndex <= 0) {
            return List.of();
        }
        String prefix = content.substring(0, splitIndex);
        try {
            JsonNode root = JsonUtils.parse(prefix);
            JsonNode files = root == null ? null : root.get("files");
            if (files == null || !files.isArray()) {
                return List.of();
            }
            List<UploadedAttachmentRef> refs = new ArrayList<>();
            for (JsonNode file : files) {
                UploadedAttachmentRef ref = new UploadedAttachmentRef(
                        text(file, "id"),
                        text(file, "name"),
                        text(file, "extension"),
                        text(file, "size"));
                if (ref.hasId()) {
                    refs.add(ref);
                }
            }
            return refs;
        } catch (RuntimeException e) {
            return List.of();
        }
    }

    private static UploadedAttachmentRef fromMap(Map<String, Object> attachment) {
        if (attachment == null || attachment.isEmpty()) {
            return null;
        }
        return new UploadedAttachmentRef(
                value(attachment.get("id")),
                value(attachment.get("name")),
                value(attachment.get("extension")),
                value(attachment.get("size")));
    }

    private static void addOrMerge(Map<String, UploadedAttachmentRef> refs, UploadedAttachmentRef candidate) {
        if (candidate == null || !candidate.hasId()) {
            return;
        }
        UploadedAttachmentRef existing = refs.get(candidate.id());
        if (existing == null) {
            refs.put(candidate.id(), candidate);
            return;
        }
        refs.put(candidate.id(), new UploadedAttachmentRef(
                candidate.id(),
                firstNonBlank(candidate.name(), existing.name()),
                firstNonBlank(candidate.extension(), existing.extension()),
                firstNonBlank(candidate.size(), existing.size())));
    }

    private static String text(JsonNode node, String fieldName) {
        if (node == null || !node.has(fieldName) || node.get(fieldName).isNull()) {
            return null;
        }
        return node.get(fieldName).asText();
    }

    private static String value(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static String firstNonBlank(String preferred, String fallback) {
        return preferred == null || preferred.isBlank() ? fallback : preferred;
    }

    private static void appendIfPresent(StringBuilder target, String label, String value) {
        if (value != null && !value.isBlank()) {
            target.append(label).append(safe(value)).append('\n');
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value.replace('\r', ' ').replace('\n', ' ').trim();
    }
}
