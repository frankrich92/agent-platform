package com.htam.agent.job.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htam.agent.common.entity.JobLog;
import com.htam.agent.job.mapper.JobLogMapper;
import org.springframework.stereotype.Service;

/**
 * @author huxuehao
 **/
@Service
public class QuartzLogServiceImpl extends ServiceImpl<JobLogMapper, JobLog> implements QuartzLogService {
}
