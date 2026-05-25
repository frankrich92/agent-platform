package com.htam.agent.adapter.rest.run;

import com.htam.agent.common.config.auth.ChatKeyAccess;
import com.htam.agent.common.config.auth.SkAccess;
import com.htam.agent.common.r.R;
import com.htam.agent.run.AgentRunLedger;
import com.htam.agent.run.AgentRunRecord;
import com.htam.agent.run.AgentRunSearchQuery;
import com.htam.agent.run.event.RuntimeEventLedger;
import com.htam.agent.run.replay.RunReplayPlan;
import com.htam.agent.run.replay.RunReplayService;
import com.htam.agent.run.step.RunStepLedger;
import com.htam.agent.run.toolcall.ToolCallLedger;
import com.htam.agent.runtime.AgentRunStatus;
import com.htam.agent.runtime.RunStep;
import com.htam.agent.runtime.RuntimeEvent;
import com.htam.agent.runtime.ToolCall;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/agent/run")
@RequiredArgsConstructor
public class AgentRunReplayController {

    private final RunReplayService runReplayService;
    private final RuntimeEventLedger runtimeEventLedger;
    private final RunStepLedger runStepLedger;
    private final ToolCallLedger toolCallLedger;
    private final AgentRunLedger agentRunLedger;

    @SkAccess
    @ChatKeyAccess
    @GetMapping
    public R<List<AgentRunRecord>> runs(
            @RequestParam(value = "sessionId", required = false) String sessionId,
            @RequestParam(value = "status", required = false) AgentRunStatus status,
            @RequestParam(value = "q", required = false) String keyword,
            @RequestParam(value = "limit", required = false, defaultValue = "50") int limit) {
        return R.data(agentRunLedger.search(new AgentRunSearchQuery(sessionId, status, keyword, limit)));
    }

    @SkAccess
    @ChatKeyAccess
    @GetMapping("/{runId}")
    public R<AgentRunRecord> run(@PathVariable("runId") String runId) {
        return R.data(agentRunLedger.findById(runId).orElse(null));
    }

    @SkAccess
    @ChatKeyAccess
    @GetMapping("/{runId}/replay")
    public R<RunReplayPlan> replay(@PathVariable("runId") String runId) {
        return R.data(runReplayService.createReplay(runId));
    }

    @SkAccess
    @ChatKeyAccess
    @GetMapping("/{runId}/events")
    public R<List<RuntimeEvent>> events(@PathVariable("runId") String runId) {
        return R.data(runtimeEventLedger.listByRunId(runId));
    }

    @SkAccess
    @ChatKeyAccess
    @GetMapping("/{runId}/steps")
    public R<List<RunStep>> steps(@PathVariable("runId") String runId) {
        return R.data(runStepLedger.listByRunId(runId));
    }

    @SkAccess
    @ChatKeyAccess
    @GetMapping("/{runId}/tool-calls")
    public R<List<ToolCall>> toolCalls(@PathVariable("runId") String runId) {
        return R.data(toolCallLedger.listByRunId(runId));
    }
}
