package com.htam.agent.hook.mapper;

import com.htam.agent.common.entity.AgentHook;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 智能体Hook关联Mapper
 *
 * @author huxuehao
 */
@Mapper
public interface AgentHookMapper extends BaseMapper<AgentHook> {
}
