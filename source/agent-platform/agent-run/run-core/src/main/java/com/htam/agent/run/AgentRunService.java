package com.htam.agent.run;

/**
 * Agent 运行应用服务入口。
 * 负责把一次运行命令写入运行台账，并触发后续运行时执行与状态汇总。
 */
public interface AgentRunService {

    AgentRunSummary run(AgentRunCommand command);
}
