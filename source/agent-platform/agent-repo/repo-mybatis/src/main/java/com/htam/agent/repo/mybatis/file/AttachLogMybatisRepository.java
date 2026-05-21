package com.htam.agent.repo.mybatis.file;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.htam.agent.common.entity.AttachLog;
import com.htam.agent.common.mp.support.MP;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.repo.file.AttachLogRepository;
import com.htam.agent.repo.support.RepoPage;
import com.htam.agent.resource.mapper.AttachLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AttachLogMybatisRepository implements AttachLogRepository {
    private final AttachLogMapper attachLogMapper;

    @Override
    public RepoPage<AttachLog> page(PageParams pageParams,
                                    Long fileId,
                                    String originalName,
                                    String extension,
                                    String optType) {
        IPage<AttachLog> page = attachLogMapper.selectPage(MP.getPage(pageParams),
                Wrappers.<AttachLog>lambdaQuery()
                        .eq(fileId != null, AttachLog::getFileId, fileId)
                        .like(originalName != null && !originalName.isBlank(), AttachLog::getOriginalName, originalName)
                        .like(extension != null && !extension.isBlank(), AttachLog::getExtension, extension)
                        .eq(optType != null && !optType.isBlank(), AttachLog::getOptType, optType)
                        .orderByDesc(AttachLog::getOptTime));
        return new RepoPage<>(page.getRecords(), page.getTotal(), page.getSize(), page.getCurrent());
    }

    @Override
    public boolean save(AttachLog attachLog) {
        return attachLogMapper.insert(attachLog) > 0;
    }
}
