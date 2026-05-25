package com.htam.agent.run.history;

import com.htam.agent.run.event.RuntimeEventLedger;
import com.htam.agent.run.message.RunMessageLedger;
import com.htam.agent.run.step.RunStepLedger;
import com.htam.agent.run.toolcall.ToolCallLedger;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class LedgerRunHistoryReader implements RunHistoryReader {

    private final RuntimeEventLedger eventLedger;
    private final RunStepLedger stepLedger;
    private final ToolCallLedger toolCallLedger;
    private final RunMessageLedger messageLedger;

    public LedgerRunHistoryReader(
            RuntimeEventLedger eventLedger,
            RunStepLedger stepLedger,
            ToolCallLedger toolCallLedger,
            RunMessageLedger messageLedger) {
        this.eventLedger = eventLedger;
        this.stepLedger = stepLedger;
        this.toolCallLedger = toolCallLedger;
        this.messageLedger = messageLedger;
    }

    @Autowired
    public LedgerRunHistoryReader(
            RuntimeEventLedger eventLedger,
            RunStepLedger stepLedger,
            ToolCallLedger toolCallLedger,
            ObjectProvider<RunMessageLedger> messageLedger) {
        this(eventLedger, stepLedger, toolCallLedger, messageLedger == null ? null : messageLedger.getIfAvailable());
    }

    @Override
    public RunHistoryRecord read(String runId) {
        return new RunHistoryRecord(
                runId,
                eventLedger == null ? List.of() : eventLedger.listByRunId(runId).stream()
                        .sorted(Comparator.comparingLong(event -> event.sequence()))
                        .toList(),
                stepLedger == null ? List.of() : stepLedger.listByRunId(runId),
                toolCallLedger == null ? List.of() : toolCallLedger.listByRunId(runId),
                messageLedger == null ? List.of() : messageLedger.listByRunId(runId));
    }
}
