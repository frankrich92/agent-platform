CREATE TABLE IF NOT EXISTS agent_run (
    run_id varchar(128) NOT NULL,
    agent_id bigint NULL,
    session_id varchar(128) NULL,
    status varchar(32) NOT NULL,
    input text NULL,
    output text NULL,
    trace_id varchar(128) NULL,
    started_at timestamp NULL,
    ended_at timestamp NULL,
    metadata_json text NULL,
    created_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (run_id)
);

CREATE INDEX IF NOT EXISTS idx_agent_run_agent_id ON agent_run(agent_id);
CREATE INDEX IF NOT EXISTS idx_agent_run_session_id ON agent_run(session_id);
CREATE INDEX IF NOT EXISTS idx_agent_run_status ON agent_run(status);

CREATE TABLE IF NOT EXISTS agent_run_event (
    event_id varchar(128) NOT NULL,
    run_id varchar(128) NOT NULL,
    session_id varchar(128) NULL,
    step_id varchar(128) NULL,
    trace_id varchar(128) NULL,
    sequence bigint NOT NULL,
    event_type varchar(64) NOT NULL,
    occurred_at timestamp NULL,
    payload_json text NULL,
    PRIMARY KEY (event_id)
);

CREATE INDEX IF NOT EXISTS idx_agent_run_event_run_seq ON agent_run_event(run_id, sequence);
CREATE INDEX IF NOT EXISTS idx_agent_run_event_type ON agent_run_event(event_type);

CREATE TABLE IF NOT EXISTS agent_run_step (
    step_id varchar(128) NOT NULL,
    run_id varchar(128) NOT NULL,
    step_type varchar(64) NOT NULL,
    status varchar(32) NOT NULL,
    started_at timestamp NULL,
    ended_at timestamp NULL,
    summary_json text NULL,
    PRIMARY KEY (step_id)
);

CREATE INDEX IF NOT EXISTS idx_agent_run_step_run_id ON agent_run_step(run_id);
CREATE INDEX IF NOT EXISTS idx_agent_run_step_type ON agent_run_step(step_type);

CREATE TABLE IF NOT EXISTS agent_tool_call (
    tool_call_id varchar(128) NOT NULL,
    run_id varchar(128) NOT NULL,
    tool_name varchar(255) NULL,
    policy varchar(32) NULL,
    read_only boolean NULL DEFAULT false,
    duration_millis bigint NULL DEFAULT 0,
    cost_micros bigint NULL DEFAULT 0,
    parameter_summary text NULL,
    result_summary text NULL,
    error_code varchar(128) NULL,
    error_message text NULL,
    audit_tags_json text NULL,
    created_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (tool_call_id)
);

CREATE INDEX IF NOT EXISTS idx_agent_tool_call_run_id ON agent_tool_call(run_id);
CREATE INDEX IF NOT EXISTS idx_agent_tool_call_tool_name ON agent_tool_call(tool_name);
