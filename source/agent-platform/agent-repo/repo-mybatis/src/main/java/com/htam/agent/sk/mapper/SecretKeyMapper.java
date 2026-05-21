package com.htam.agent.sk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.htam.agent.common.entity.SecretKey;
import org.apache.ibatis.annotations.Mapper;

/**
 * 描述：访问秘钥数据访问层
 *
 * @author huxuehao
 **/
@Mapper
public interface SecretKeyMapper extends BaseMapper<SecretKey> {
}
