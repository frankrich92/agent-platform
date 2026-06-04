package com.htam.agent.runtime;

/**
 * 运行时能力装配入口。
 * 业务模块提交 Agent 配置上下文，由实现层转换为具体运行时可消费的模型、工具、MCP、知识库等能力集合。
 */
public interface RuntimeCapabilityAssembler {

    RuntimeCapabilityAssembly assemble(RuntimeCapabilityAssemblyRequest request);
}
