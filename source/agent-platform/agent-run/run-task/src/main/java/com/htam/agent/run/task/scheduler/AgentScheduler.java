package com.htam.agent.run.task.scheduler;

import com.htam.agent.common.wrapper.AgentJobWrapper;
import com.htam.agent.run.task.consts.JobConst;
import com.htam.agent.run.task.core.job.QuartzJob;
import com.htam.agent.runtime.AgentRunRequest;
import com.htam.agent.runtime.AgentRuntimeRunner;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;

/**
 * 描述：智能体任务
 *
 * @author huxuehao
 **/
@Slf4j
public class AgentScheduler extends QuartzJob {
    @Override
    public Object doJob(JobExecutionContext context) {
        AgentJobWrapper wrapper = getDataMap(JobConst.DATA_MAP_KEY, AgentJobWrapper.class);
        if (wrapper == null) {
            return false;
        }

        String agentId = wrapper.getAgentId();
        if (agentId == null || agentId.trim().isEmpty()) {
            return false;
        }

        try {
            AgentRuntimeRunner runtimeRunner = getBean(AgentRuntimeRunner.class);
            runtimeRunner.run(AgentRunRequest.of(Long.valueOf(agentId.trim()), wrapper.getInput()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
        return true;
    }
}
