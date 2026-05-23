package com.htam.agent.runtime;

/**
 * Agent 运行时入口。业务模块只依赖该接口，不直接依赖 AgentScope 等具体实现。
 */
public interface AgentRuntimeRunner {

    AgentRunResult run(AgentRunRequest request);
}
