package com.htam.agent.sensitive.mapper;

import com.htam.agent.common.entity.SensitiveWordConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 敏感词配置Mapper
 *
 * @author huxuehao
 */
@Mapper
public interface SensitiveWordConfigMapper extends BaseMapper<SensitiveWordConfig> {
}
