package com.htam.agent.runtime;

public enum RunStepType {
    MODEL_CALL,
    TOOL_CALL,
    MCP_CALL,
    SKILL_LOAD,
    KNOWLEDGE_RETRIEVAL,
    MEMORY_FLUSH,
    CONTEXT_COMPRESSION,
    SUB_AGENT,
    WORKER,
    OTHER
}
