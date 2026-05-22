package com.htam.agent.repo.mybatis.file;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.htam.agent.common.entity.Attach;
import com.htam.agent.common.mp.support.MP;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.repo.file.AttachRepository;
import com.htam.agent.repo.support.RepoPage;
import com.htam.agent.resource.mapper.AttachMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AttachMybatisRepository implements AttachRepository {
    private final AttachMapper attachMapper;

    @Override
    public RepoPage<Attach> page(PageParams pageParams,
                                 Long fileId,
                                 String link,
                                 String domain,
                                 String name,
                                 String originalName,
                                 String extension) {
        IPage<Attach> page = attachMapper.selectPage(MP.getPage(pageParams),
                Wrappers.<Attach>lambdaQuery()
                        .eq(fileId != null, Attach::getFileId, fileId)
                        .like(link != null && !link.isBlank(), Attach::getLink, link)
                        .like(domain != null && !domain.isBlank(), Attach::getDomain, domain)
                        .like(name != null && !name.isBlank(), Attach::getName, name)
                        .like(originalName != null && !originalName.isBlank(), Attach::getOriginalName, originalName)
                        .like(extension != null && !extension.isBlank(), Attach::getExtension, extension)
                        .orderByDesc(Attach::getCreateAt));
        return new RepoPage<>(page.getRecords(), page.getTotal(), page.getSize(), page.getCurrent());
    }

    @Override
    public Attach getById(Long id) {
        return attachMapper.selectById(id);
    }

    @Override
    public List<Attach> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return attachMapper.selectBatchIds(ids);
    }

    @Override
    public boolean save(Attach attach) {
        return attachMapper.insert(attach) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return attachMapper.deleteById(id) > 0;
    }
}
