package com.htam.agent.skill.imports.source;

import cn.hutool.core.io.FileUtil;
import com.htam.agent.common.exception.BusinessException;
import com.htam.agent.common.runtime.RuntimePaths;
import com.htam.agent.common.util.FolderUtils;
import com.htam.agent.common.util.ZipExtractUtils;
import com.htam.agent.skill.imports.ImportedSkill;
import com.htam.agent.skill.imports.SkillImportPathResolver;
import io.agentscope.core.skill.repository.FileSystemSkillRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class UploadSkillImportSource implements SkillImportSource {
    private final Path extractDir;
    private final Path skillsDir;
    private final FileSystemSkillRepository repository;

    public UploadSkillImportSource(MultipartFile file) throws IOException {
        this.extractDir = RuntimePaths.createTempDir();
        Path tempZip = extractDir.resolveSibling(extractDir.getFileName() + ".zip");
        try {
            ZipExtractUtils.extractZipSafely(file.getInputStream(), extractDir, tempZip);
            this.skillsDir = SkillImportPathResolver.resolveUploadedSkillsDir(extractDir);
            this.repository = new FileSystemSkillRepository(skillsDir);
        } catch (IOException e) {
            FileUtil.del(extractDir.toFile());
            throw new BusinessException("压缩包解压失败，请确认文件为有效 zip 格式: " + e.getMessage());
        } catch (RuntimeException e) {
            FileUtil.del(extractDir.toFile());
            throw e;
        } finally {
            Files.deleteIfExists(tempZip);
        }
    }

    @Override
    public Path skillsDir() {
        return skillsDir;
    }

    @Override
    public List<String> skillNames() {
        return repository.getAllSkillNames();
    }

    @Override
    public ImportedSkill skill(String skillName) {
        return AgentScopeSkillMapper.fromAgentScope(repository.getSkill(skillName));
    }

    @Override
    public void close() {
        repository.close();
        FolderUtils.deleteRecursively(extractDir.toAbsolutePath().toString());
        log.info("清理上传临时目录: {}", extractDir.toAbsolutePath());
    }
}
