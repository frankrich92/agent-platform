package com.htam.agent.resource.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.htam.agent.common.entity.AttachLog;
import com.htam.agent.common.mp.support.PageParams;

/**
 * 描述：附件操作日志
 *
 * @author huxuehao
 **/
public interface AttachLogService {
    IPage<AttachLog> page(PageParams pageParams, AttachLog query);

    boolean save(AttachLog attachLog);
}
