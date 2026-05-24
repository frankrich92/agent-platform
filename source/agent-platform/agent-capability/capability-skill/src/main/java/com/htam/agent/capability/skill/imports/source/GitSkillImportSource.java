package com.htam.agent.capability.skill.imports.source;

import com.htam.agent.common.runtime.RuntimePaths;
import com.htam.agent.common.util.FolderUtils;
import com.htam.agent.capability.skill.imports.ImportedSkill;
import com.htam.agent.capability.skill.imports.SkillPackageReader;
import com.htam.agent.capability.skill.imports.SkillImportPathResolver;
import com.htam.agent.capability.skill.imports.config.GitImportConfig;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class GitSkillImportSource implements SkillImportSource {
    private final Path tempDir;
    private final Path skillsDir;
    private final Git git;

    public GitSkillImportSource(GitImportConfig config) throws IOException {
        Path createdTempDir = RuntimePaths.createTempDir();
        Git createdGit = null;
        try {
            createdGit = Git.cloneRepository()
                    .setURI(config.getRepoUrl())
                    .setDirectory(createdTempDir.toFile())
                    .call();
            this.skillsDir = SkillImportPathResolver.resolveSkillsDir(createdTempDir);
            this.tempDir = createdTempDir;
            this.git = createdGit;
        } catch (Exception e) {
            closeQuietly(createdGit);
            FolderUtils.deleteRecursively(createdTempDir.toAbsolutePath().toString());
            throw new IOException("克隆 Git 技能仓库失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Path skillsDir() {
        return skillsDir;
    }

    @Override
    public List<String> skillNames() {
        return SkillPackageReader.skillNames(skillsDir);
    }

    @Override
    public ImportedSkill skill(String skillName) {
        return SkillPackageReader.read(skillsDir, skillName);
    }

    @Override
    public void close() {
        closeQuietly(git);
        FolderUtils.deleteRecursively(tempDir.toAbsolutePath().toString());
        log.info("清理 Git 临时目录: {}", tempDir.toAbsolutePath());
    }

    private void closeQuietly(Git gitRepository) {
        if (gitRepository == null) {
            return;
        }
        try {
            gitRepository.close();
        } catch (Exception e) {
            log.warn("关闭 Git 仓库临时目录时出现文件占用（Windows 环境可忽略）：{}", e.getMessage());
        }
    }
}
