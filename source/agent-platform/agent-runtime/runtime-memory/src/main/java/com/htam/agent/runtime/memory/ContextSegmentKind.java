package com.htam.agent.runtime.memory;

public enum ContextSegmentKind {
    SYSTEM_PROMPT,
    USER_INPUT,
    SESSION_HISTORY,
    LONG_TERM_MEMORY,
    TOOL_RESULT,
    RUNTIME_HINT
}
