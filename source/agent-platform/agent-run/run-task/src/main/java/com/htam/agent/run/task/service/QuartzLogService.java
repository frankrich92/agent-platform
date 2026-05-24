package com.htam.agent.run.task.service;

import com.htam.agent.common.entity.JobLog;

/**
 * @author huxuehao
 **/
public interface QuartzLogService {
    boolean save(JobLog jobLog);
}
