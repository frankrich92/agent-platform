package com.htam.agent.run.session;

import java.util.List;
import java.util.Optional;

public interface RunSessionLedger {

    RunSessionRecord save(RunSessionRecord session);

    Optional<RunSessionRecord> findById(String sessionId);

    List<RunSessionRecord> listByAgentId(Long agentId);
}
