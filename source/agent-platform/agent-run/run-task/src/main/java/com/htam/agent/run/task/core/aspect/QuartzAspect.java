package com.htam.agent.run.task.core.aspect;

import com.htam.agent.common.entity.JobInfo;
import com.htam.agent.run.task.service.QuartzInfoService;

/**
 * 描述：切面抽象类
 *
 * @author huxuehao
 **/
public abstract class QuartzAspect {
    private final QuartzInfoService quartzStatusService;
    public QuartzAspect(QuartzInfoService quartzStatusService) {
        this.quartzStatusService = quartzStatusService;
    }

    public void saveStatus(JobInfo jobStatus) {
        quartzStatusService.updateStatus(jobStatus);
    }
}
