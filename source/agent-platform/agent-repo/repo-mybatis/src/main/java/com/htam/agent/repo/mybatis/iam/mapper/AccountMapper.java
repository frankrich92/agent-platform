package com.htam.agent.repo.mybatis.iam.mapper;

import com.htam.agent.common.entity.Account;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 账号Mapper
 *
 * @author huxuehao
 */
@Mapper
public interface AccountMapper extends BaseMapper<Account> {
}
