package com.htam.agent.job.core.aspect;

import com.htam.agent.job.core.config.QuartzConfig;
import com.htam.agent.job.core.enums.QuartzStatus;
import com.htam.agent.common.entity.JobInfo;
import com.htam.agent.job.service.QuartzInfoService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 描述：创建调度任务切面
 *
 * @author huxuehao
 **/
@Aspect
@Order(-1) /* 该切面应当先于 @Transactional 执行 */
@Component
public class QuartzCreateAspect extends QuartzAspect {
    public QuartzCreateAspect(QuartzInfoService quartzStatusService) {
        super(quartzStatusService);
    }

    @Pointcut("@annotation(com.htam.agent.job.core.annotation.QuartzCreate)")
    public void quartzCreatePointcut() {

    }

    @AfterReturning(pointcut = "quartzCreatePointcut()", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Object result) {
        if (result == null) return;
        QuartzConfig config = (QuartzConfig)result;
        JobInfo jobInfo = new JobInfo();
        jobInfo.setId(config.getIdentity());
        jobInfo.setCron(config.getCron());
        jobInfo.setEnabled(QuartzStatus.START.value());
        saveStatus(jobInfo);
    }
}
