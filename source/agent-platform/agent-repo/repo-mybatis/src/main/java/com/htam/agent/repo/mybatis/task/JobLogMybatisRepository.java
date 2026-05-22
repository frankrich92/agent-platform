package com.htam.agent.repo.mybatis.task;

import com.htam.agent.common.entity.JobLog;
import com.htam.agent.job.mapper.JobLogMapper;
import com.htam.agent.repo.task.JobLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JobLogMybatisRepository implements JobLogRepository {
    private final JobLogMapper jobLogMapper;

    @Override
    public boolean save(JobLog jobLog) {
        return jobLogMapper.insert(jobLog) > 0;
    }
}
