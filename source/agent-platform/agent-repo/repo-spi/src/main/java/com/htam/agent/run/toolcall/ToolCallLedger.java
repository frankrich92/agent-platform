package com.htam.agent.run.toolcall;

import com.htam.agent.runtime.ToolCall;
import java.util.List;

public interface ToolCallLedger extends ToolCallSink {

    List<ToolCall> listByRunId(String runId);
}
