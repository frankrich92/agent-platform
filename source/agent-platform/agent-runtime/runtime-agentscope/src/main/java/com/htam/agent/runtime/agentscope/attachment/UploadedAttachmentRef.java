package com.htam.agent.runtime.agentscope.attachment;

public record UploadedAttachmentRef(
        String id,
        String name,
        String extension,
        String size) {

    public boolean hasId() {
        return id != null && !id.isBlank();
    }
}
