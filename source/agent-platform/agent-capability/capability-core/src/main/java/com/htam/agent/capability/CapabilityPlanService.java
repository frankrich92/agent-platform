package com.htam.agent.capability;

/**
 * Agent 能力计划解析服务。
 * 该接口把 Profile 侧的配置态绑定转换为运行前可评估、可治理的能力清单。
 */
public interface CapabilityPlanService {

    CapabilityPlan resolvePlan(Long agentId);
}
