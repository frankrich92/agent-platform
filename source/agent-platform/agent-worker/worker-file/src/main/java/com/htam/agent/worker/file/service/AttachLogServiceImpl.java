package com.htam.agent.worker.file.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htam.agent.common.entity.AttachLog;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.repo.file.AttachLogRepository;
import com.htam.agent.repo.support.RepoPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 描述：附件操作日志
 *
 * @author huxuehao
 **/
@Service
@RequiredArgsConstructor
public class AttachLogServiceImpl implements AttachLogService {
    private final AttachLogRepository attachLogRepository;

    @Override
    public IPage<AttachLog> page(PageParams pageParams, AttachLog query) {
        AttachLog attachLog = query == null ? new AttachLog() : query;
        RepoPage<AttachLog> repoPage = attachLogRepository.page(
                pageParams,
                attachLog.getFileId(),
                attachLog.getOriginalName(),
                attachLog.getExtension(),
                attachLog.getOptType());
        IPage<AttachLog> page = new Page<>(repoPage.current(), repoPage.size(), repoPage.total());
        page.setRecords(repoPage.records());
        return page;
    }

    @Override
    public boolean save(AttachLog attachLog) {
        return attachLogRepository.save(attachLog);
    }
}
