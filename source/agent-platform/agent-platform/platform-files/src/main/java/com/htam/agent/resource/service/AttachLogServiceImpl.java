package com.htam.agent.resource.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htam.agent.common.entity.AttachLog;
import com.htam.agent.resource.mapper.AttachLogMapper;
import org.springframework.stereotype.Service;

/**
 * 描述：附件操作日志
 *
 * @author huxuehao
 **/
@Service
public class AttachLogServiceImpl extends ServiceImpl<AttachLogMapper, AttachLog> implements AttachLogService {
}
