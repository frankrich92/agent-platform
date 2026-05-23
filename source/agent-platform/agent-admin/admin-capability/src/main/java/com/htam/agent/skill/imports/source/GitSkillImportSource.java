package com.htam.agent.skill.imports.source;

import com.htam.agent.common.runtime.RuntimePaths;
import com.htam.agent.common.util.FolderUtils;
import com.htam.agent.skill.imports.SkillImportPathResolver;
import com.htam.agent.skill.imports.config.GitImportConfig;
import io.agentscope.core.skill.repository.AgentSkillRepository;
import io.agentscope.core.skill.repository.GitSkillRepository;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;

@Slf4j
public class GitSkillImportSource implements SkillImportSource {
    private final Path tempDir;
    private final Path skillsDir;
    private final GitSkillRepository repository;

    public GitSkillImportSource(GitImportConfig config) throws IOException {
        Path createdTempDir = RuntimePaths.createTempDir();
        GitSkillRepository createdRepository = null;
        try {
            createdRepository = new GitSkillRepository(config.getRepoUrl(), createdTempDir);
            createdRepository.sync();
            this.skillsDir = SkillImportPathResolver.resolveSkillsDir(createdTempDir);
            this.tempDir = createdTempDir;
            this.repository = createdRepository;
        } catch (RuntimeException e) {
            closeQuietly(createdRepository);
            FolderUtils.deleteRecursively(createdTempDir.toAbsolutePath().toString());
            throw e;
        }
    }

    @Override
    public Path skillsDir() {
        return skillsDir;
    }

    @Override
    public AgentSkillRepository repository() {
        return repository;
    }

    @Override
    public void close() {
        closeQuietly(repository);
        FolderUtils.deleteRecursively(tempDir.toAbsolutePath().toString());
        log.info("清理 Git 临时目录: {}", tempDir.toAbsolutePath());
    }

    private void closeQuietly(GitSkillRepository repo) {
        if (repo == null) {
            return;
        }
        try {
            repo.close();
        } catch (Exception e) {
            log.warn("关闭 Git 仓库临时目录时出现文件占用（Windows 环境可忽略）：{}", e.getMessage());
        }
    }
}
