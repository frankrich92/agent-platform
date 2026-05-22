package com.htam.agent.repo.agent;

import com.htam.agent.common.entity.JobInfo;
import java.util.List;

public interface JobInfoRepository {
    List<JobInfo> list();

    List<JobInfo> listEnabled();

    JobInfo getById(String id);

    JobInfo getAgentJobByBizId(String bizId);

    boolean save(JobInfo jobInfo);

    boolean updateById(JobInfo jobInfo);

    boolean updateStatus(String id, boolean enabled);

    boolean deleteById(String id);

    List<JobInfo> listAgentJobs();

    List<JobInfo> listAgentJobsByBizId(Long bizId);

    List<JobInfo> listAgentJobsByBizIds(List<Long> bizIds);
}
