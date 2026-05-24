package com.htam.agent.capability.skill.imports.source;

import cn.hutool.core.io.FileUtil;
import com.htam.agent.common.exception.BusinessException;
import com.htam.agent.common.runtime.RuntimePaths;
import com.htam.agent.common.util.FolderUtils;
import com.htam.agent.common.util.ZipExtractUtils;
import com.htam.agent.capability.skill.imports.ImportedSkill;
import com.htam.agent.capability.skill.imports.SkillPackageReader;
import com.htam.agent.capability.skill.imports.SkillImportPathResolver;
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

    public UploadSkillImportSource(MultipartFile file) throws IOException {
        this.extractDir = RuntimePaths.createTempDir();
        Path tempZip = extractDir.resolveSibling(extractDir.getFileName() + ".zip");
        try {
            ZipExtractUtils.extractZipSafely(file.getInputStream(), extractDir, tempZip);
            this.skillsDir = SkillImportPathResolver.resolveUploadedSkillsDir(extractDir);
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
        return SkillPackageReader.skillNames(skillsDir);
    }

    @Override
    public ImportedSkill skill(String skillName) {
        return SkillPackageReader.read(skillsDir, skillName);
    }

    @Override
    public void close() {
        FolderUtils.deleteRecursively(extractDir.toAbsolutePath().toString());
        log.info("清理上传临时目录: {}", extractDir.toAbsolutePath());
    }
}
