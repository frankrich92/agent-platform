package com.htam.agent.repo.task;

import com.htam.agent.common.entity.JobLog;

public interface JobLogRepository {
    boolean save(JobLog jobLog);
}
