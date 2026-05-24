package com.htam.agent.worker.shell;

import com.htam.agent.worker.spi.WorkerTaskResult;
import com.htam.agent.worker.spi.WorkerTaskStatus;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ShellCommandExecutor {

    public WorkerTaskResult execute(ShellCommandRequest request) {
        Instant startedAt = Instant.now();
        ProcessBuilder builder = new ProcessBuilder(request.command());
        if (request.workingDirectory() != null) {
            builder.directory(request.workingDirectory().toFile());
        }
        try {
            Process process = builder.start();
            boolean finished = process.waitFor(request.timeout().toMillis(), TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                return result(startedAt, WorkerTaskStatus.FAILED, null, "WORKER_TIMEOUT", "command timed out");
            }
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            String error = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            if (process.exitValue() == 0) {
                return result(startedAt, WorkerTaskStatus.SUCCEEDED, summarize(output), null, null);
            }
            return result(startedAt, WorkerTaskStatus.FAILED, summarize(output), "COMMAND_FAILED", summarize(error));
        } catch (IOException e) {
            return result(startedAt, WorkerTaskStatus.FAILED, null, "COMMAND_IO_ERROR", e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return result(startedAt, WorkerTaskStatus.CANCELLED, null, "COMMAND_INTERRUPTED", e.getMessage());
        }
    }

    private static WorkerTaskResult result(
            Instant startedAt,
            WorkerTaskStatus status,
            String outputSummary,
            String errorCode,
            String errorMessage) {
        return new WorkerTaskResult(UUID.randomUUID().toString(), status, Duration.between(startedAt, Instant.now()),
                outputSummary, errorCode, errorMessage, Map.of("executor", "shell"));
    }

    private static String summarize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.strip();
        return normalized.length() <= 2000 ? normalized : normalized.substring(0, 2000);
    }
}
