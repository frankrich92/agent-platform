package com.htam.agent.job.service;

import com.htam.agent.common.entity.JobLog;
import com.htam.agent.repo.task.JobLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author huxuehao
 **/
@Service
@RequiredArgsConstructor
public class QuartzLogServiceImpl implements QuartzLogService {
    private final JobLogRepository jobLogRepository;

    @Override
    public boolean save(JobLog jobLog) {
        return jobLogRepository.save(jobLog);
    }
}
