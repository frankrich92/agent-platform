package com.htam.agent.run.task.init;

import com.htam.agent.common.util.JsonUtils;
import com.htam.agent.common.wrapper.AgentJobWrapper;
import com.htam.agent.run.task.consts.JobConst;
import com.htam.agent.run.task.core.client.QuartzClient;
import com.htam.agent.run.task.core.config.QuartzConfig;
import com.htam.agent.run.task.core.config.QuartzConfigFactory;
import com.htam.agent.run.task.core.job.QuartzJob;
import com.htam.agent.common.entity.JobInfo;
import com.htam.agent.run.task.service.QuartzInfoService;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 描述：初始化定时任务
 *
 * @author huxuehao
 **/
@Component
public class JobInit implements SmartInitializingSingleton {

    private final QuartzClient quartzClient;
    private final QuartzInfoService quartzInfoService;

    public JobInit(QuartzClient quartzClient, QuartzInfoService quartzInfoService) {
        this.quartzClient = quartzClient;
        this.quartzInfoService = quartzInfoService;
    }

    public void doJobInit() {
        List<JobInfo> list = quartzInfoService.listEnabled();
        for (JobInfo jobInfo : list) {
            try {
                quartzClient.create(buildConfig(jobInfo));
            } catch (ClassNotFoundException e) {
                System.err.println(jobInfo.getJobClass() + "不存在");
            }
        }
    }

    public static QuartzConfig buildConfig(JobInfo jobInfo) throws ClassNotFoundException {
        Class<?> aClass = Class.forName(jobInfo.getJobClass());
        if (QuartzJob.class.isAssignableFrom(aClass)) {
            Class<? extends QuartzJob> jobClass = aClass.asSubclass(QuartzJob.class);
            return new QuartzConfigFactory()
                    // 设置唯一标识，一般是ID
                    .identity(jobInfo.getId())
                    // 设置自定义doJob类
                    .setJobClass(jobClass)
                    // 可以再自定义job中取到在此传递的值
                    .putDataMap(
                            JobConst.DATA_MAP_KEY,
                            JsonUtils.parse(jobInfo.getDataMap(), AgentJobWrapper.class))
                    // 设置cron表达式
                    .cron(jobInfo.getCron())
                    // 获取到config
                    .build();
        } else {
            throw new RuntimeException("类型不兼容，无法转换：" + jobInfo.getJobClass() + "未继承自QuartzJob");
        }
    }

    @Override
    public void afterSingletonsInstantiated() {
        doJobInit();
    }
}
