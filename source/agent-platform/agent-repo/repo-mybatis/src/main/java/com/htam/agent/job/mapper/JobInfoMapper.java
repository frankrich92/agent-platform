package com.htam.agent.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.htam.agent.common.entity.JobInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author huxuehao
 **/
@Mapper
public interface JobInfoMapper extends BaseMapper<JobInfo> {
}
