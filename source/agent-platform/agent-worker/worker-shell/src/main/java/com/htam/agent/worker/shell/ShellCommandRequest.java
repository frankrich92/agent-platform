package com.htam.agent.worker.shell;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

public record ShellCommandRequest(
        List<String> command,
        Path workingDirectory,
        Duration timeout) {

    public ShellCommandRequest {
        if (command == null || command.isEmpty()) {
            throw new IllegalArgumentException("command 不能为空");
        }
        command = List.copyOf(command);
        timeout = timeout == null ? Duration.ofSeconds(30) : timeout;
    }
}
