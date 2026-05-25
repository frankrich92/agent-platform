package com.htam.agent.common.file;

import com.htam.agent.common.wrapper.FileBase64Wrapper;

public interface AttachmentContentReader {
    FileBase64Wrapper getFileBase64(Long fileId);
}
