package com.htam.agent.tool.mapper;

import com.htam.agent.common.entity.ToolConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工具Mapper
 *
 * @author huxuehao
 */
@Mapper
public interface ToolMapper extends BaseMapper<ToolConfig> {
}
