CREATE UNIQUE INDEX IF NOT EXISTS uk_agent_run_event_run_sequence
    ON agent_run_event(run_id, sequence);
