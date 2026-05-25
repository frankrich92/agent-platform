package com.htam.agent.workflow.human;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface HumanTaskLedger {

    HumanTask create(HumanTask task);

    HumanTask complete(String taskId, Map<String, Object> payload);

    Optional<HumanTask> findById(String taskId);

    List<HumanTask> listPendingByRunId(String runId);

}
