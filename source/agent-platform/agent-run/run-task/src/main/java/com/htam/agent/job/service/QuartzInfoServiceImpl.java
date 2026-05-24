package com.htam.agent.job.service;

import com.htam.agent.profile.agent.service.AgentDefinitionService;
import com.htam.agent.common.entity.AgentDefinition;
import com.htam.agent.common.entity.JobInfo;
import com.htam.agent.common.util.CryptoUtils;
import com.htam.agent.job.core.client.QuartzClient;
import com.htam.agent.job.init.JobInit;
import com.htam.agent.repo.agent.JobInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 描述：定时任务管理服务实现
 *
 * @author huxuehao
 **/
@Service
@RequiredArgsConstructor
public class QuartzInfoServiceImpl implements QuartzInfoService {

    private final QuartzClient quartzClient;
    private final AgentDefinitionService agentDefinitionService;
    private final JobInfoRepository jobInfoRepository;

    @Override
    public List<JobInfo> list() {
        return jobInfoRepository.list();
    }

    @Override
    public List<JobInfo> listEnabled() {
        return jobInfoRepository.listEnabled();
    }

    @Override
    public JobInfo getById(String id) {
        return jobInfoRepository.getById(id);
    }

    @Override
    public JobInfo getAgentJobByBizId(String bizId) {
        return jobInfoRepository.getAgentJobByBizId(bizId);
    }

    @Override
    public void updateStatus(JobInfo jobStatus) {
        jobInfoRepository.updateStatus(jobStatus.getId(), jobStatus.isEnabled());
    }

    @Override
    public void addJob(JobInfo jobInfo) {
        checkAgent(jobInfo.getBizId());
        jobInfo.setId(CryptoUtils.uuid());
        try {
            jobInfoRepository.save(jobInfo);
            if (jobInfo.isEnabled()) {
                quartzClient.create(JobInit.buildConfig(jobInfo));
            }
        } catch (Exception e) {
            jobInfoRepository.deleteById(jobInfo.getId());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateJob(JobInfo jobInfo) throws ClassNotFoundException {
        JobInfo job = getById(jobInfo.getId());
        if (job == null) {
            throw new RuntimeException("任务实例不存在");
        }
        checkAgent(job.getBizId());
        if (jobInfo.isEnabled()) {
            quartzClient.remove(JobInit.buildConfig(jobInfo));
            quartzClient.create(JobInit.buildConfig(jobInfo));
        } else {
            quartzClient.remove(JobInit.buildConfig(jobInfo));
        }
        jobInfoRepository.updateById(jobInfo);
    }

    @Override
    public void updateJobCron(String id, String cron) throws ClassNotFoundException {
        JobInfo job = getById(id);
        if (job == null) {
            throw new RuntimeException("任务实例不存在");
        }
        checkAgent(job.getBizId());
        if (cron.equals(job.getCron())) {
            return;
        }
        job.setCron(cron);
        if (job.isEnabled()) {
            quartzClient.remove(JobInit.buildConfig(job));
            quartzClient.create(JobInit.buildConfig(job));
        }
        jobInfoRepository.updateById(job);
    }

    @Override
    public boolean deleteJob(String id) throws ClassNotFoundException {
        JobInfo jobInfo = getById(id);
        if (jobInfo == null) {
            return true;
        }
        if (jobInfo.isEnabled()) {
            quartzClient.remove(JobInit.buildConfig(jobInfo));
        }
        return jobInfoRepository.deleteById(id);
    }

    @Override
    public void startJob(String id) throws ClassNotFoundException {
        JobInfo jobInfo = getById(id);
        if (jobInfo == null) {
            throw new RuntimeException("任务实例不存在");
        }
        checkAgent(jobInfo.getBizId());
        quartzClient.remove(JobInit.buildConfig(jobInfo));
        quartzClient.create(JobInit.buildConfig(jobInfo));
    }

    @Override
    public void stopJob(String id) throws ClassNotFoundException {
        JobInfo jobInfo = getById(id);
        if (jobInfo == null) {
            throw new RuntimeException("任务实例不存在");
        }
        if (jobInfo.isEnabled()) {
            return;
        }
        quartzClient.remove(JobInit.buildConfig(jobInfo));
    }

    private void checkAgent(String agentId) {
        if (agentId == null) {
            return;
        }
        AgentDefinition agentDefinition = agentDefinitionService.getById(Long.valueOf(agentId));
        if (agentDefinition != null && !agentDefinition.getEnabled()) {
            throw new RuntimeException("智能体无效，不可设置定时");
        }
    }
}
