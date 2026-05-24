package com.htam.agent.repo.mybatis.agent;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.htam.agent.repo.mybatis.agent.mapper.IJobInfoMapper;
import com.htam.agent.common.entity.JobInfo;
import com.htam.agent.repo.agent.JobInfoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JobInfoMybatisRepository implements JobInfoRepository {
    private final IJobInfoMapper jobInfoMapper;

    @Override
    public List<JobInfo> list() {
        return jobInfoMapper.selectList(Wrappers.emptyWrapper());
    }

    @Override
    public List<JobInfo> listEnabled() {
        return jobInfoMapper.selectList(Wrappers.<JobInfo>lambdaQuery()
                .eq(JobInfo::isEnabled, true));
    }

    @Override
    public JobInfo getById(String id) {
        return jobInfoMapper.selectById(id);
    }

    @Override
    public JobInfo getAgentJobByBizId(String bizId) {
        return jobInfoMapper.selectOne(Wrappers.<JobInfo>lambdaQuery()
                .eq(JobInfo::getBizId, bizId)
                .eq(JobInfo::getType, "AGENT"));
    }

    @Override
    public boolean save(JobInfo jobInfo) {
        return jobInfoMapper.insert(jobInfo) > 0;
    }

    @Override
    public boolean updateById(JobInfo jobInfo) {
        return jobInfoMapper.updateById(jobInfo) > 0;
    }

    @Override
    public boolean updateStatus(String id, boolean enabled) {
        return jobInfoMapper.update(null, Wrappers.<JobInfo>lambdaUpdate()
                .eq(JobInfo::getId, id)
                .set(JobInfo::isEnabled, enabled)) > 0;
    }

    @Override
    public boolean deleteById(String id) {
        return jobInfoMapper.deleteById(id) > 0;
    }

    @Override
    public List<JobInfo> listAgentJobs() {
        return jobInfoMapper.selectList(Wrappers.<JobInfo>lambdaQuery()
                .eq(JobInfo::getType, "AGENT"));
    }

    @Override
    public List<JobInfo> listAgentJobsByBizId(Long bizId) {
        return jobInfoMapper.selectList(Wrappers.<JobInfo>lambdaQuery()
                .eq(JobInfo::getType, "AGENT")
                .eq(JobInfo::getBizId, String.valueOf(bizId)));
    }

    @Override
    public List<JobInfo> listAgentJobsByBizIds(List<Long> bizIds) {
        if (bizIds == null || bizIds.isEmpty()) {
            return List.of();
        }
        List<String> bizIdValues = bizIds.stream()
                .map(String::valueOf)
                .toList();
        return jobInfoMapper.selectList(Wrappers.<JobInfo>lambdaQuery()
                .eq(JobInfo::getType, "AGENT")
                .in(JobInfo::getBizId, bizIdValues));
    }
}
