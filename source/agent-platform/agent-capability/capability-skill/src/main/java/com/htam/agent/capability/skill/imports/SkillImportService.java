package com.htam.agent.capability.skill.imports;

import com.htam.agent.common.entity.SkillPackage;
import com.htam.agent.common.vo.SkillImportResult;
import com.htam.agent.capability.skill.SkillScriptLoadHelper;
import com.htam.agent.capability.skill.imports.config.GitImportConfig;
import com.htam.agent.capability.skill.imports.config.LocalImportConfig;
import com.htam.agent.capability.skill.imports.source.GitSkillImportSource;
import com.htam.agent.capability.skill.imports.source.LocalSkillImportSource;
import com.htam.agent.capability.skill.imports.source.SkillImportSource;
import com.htam.agent.capability.skill.imports.source.UploadSkillImportSource;
import com.htam.agent.capability.skill.service.SkillPackageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * 描述：技能包导入服务，编排本地/压缩包/Git三种导入方式
 *
 * @author huxuehao
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class SkillImportService {
    private final SkillPackageService skillPackageService;

    /**
     * 从 Git 导入
     * 使用临时目录克隆仓库，导入完成后清理临时目录
     *
     * @param config 配置
     */
    public SkillImportResult importFromGit(GitImportConfig config) {
        try (SkillImportSource source = new GitSkillImportSource(config)) {
            return doImport(source, config.isCover(), config.getCategory());
        } catch (IOException e) {
            throw new IllegalStateException("Git 技能包导入失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从本地导入
     * 当本地路径与 SKILLS_DIR 相同时，跳过文件复制，仅更新 DB
     *
     * @param config 配置
     */
    public SkillImportResult importFromLocal(LocalImportConfig config) {
        try (SkillImportSource source = new LocalSkillImportSource(config)) {
            return doImport(source, config.isCover(), config.getCategory());
        }
    }

    /**
     * 从上传压缩包导入
     *
     * @param file     技能包压缩包
     * @param category 技能分类
     * @param cover    是否覆盖
     */
    public SkillImportResult importFromUpload(MultipartFile file, String category, boolean cover) throws IOException {
        try (SkillImportSource source = new UploadSkillImportSource(file)) {
            return doImport(source, cover, category);
        }
    }

    /**
     * 执行导入
     *
     * @param source    导入源
     * @param isCover   是否覆盖
     * @param category  分类
     */
    private SkillImportResult doImport(SkillImportSource source, boolean isCover, String category) {
        Path skillsDir = source.skillsDir();
        try {
            SkillImportNormalizer.normalizeSkillFiles(skillsDir);
        } catch (IOException e) {
            log.warn("Normalize skill files failed: {}", e.getMessage());
        }

        List<String> allSkillNames = source.skillNames();
        if (allSkillNames.isEmpty()) {
            return SkillImportResult.withHint(0, 0, 0, SkillImportInspector.buildHint(skillsDir));
        }

        int importedCount = 0;
        int skippedCount = 0;

        for (String skillName : allSkillNames) {
            Optional<Path> sourceSkillDir = SkillImportInspector.findSkillDirectory(skillsDir, skillName);
            if (sourceSkillDir.isEmpty()) {
                log.warn("技能 {} 源目录未找到，跳过安装", skillName);
                skippedCount++;
                continue;
            }

            boolean installed = SkillInstaller.install(sourceSkillDir.get(), skillName, isCover);
            if (!installed) {
                log.info("技能包 {} 已存在且跳过覆盖", skillName);
                skippedCount++;
                continue;
            }

            ImportedSkill importedSkill = source.skill(skillName);
            SkillPackage skillPackage = SkillPackageBuilder.build(importedSkill, category);

            SkillPackage oldSkillPackage = skillPackageService.getByName(skillName);

            if (oldSkillPackage == null) {
                skillPackageService.save(skillPackage);
            } else {
                skillPackage.setId(oldSkillPackage.getId());
                skillPackage.setEnabled(oldSkillPackage.getEnabled() != null ? oldSkillPackage.getEnabled() : Boolean.TRUE);
                skillPackageService.updateById(skillPackage);
            }

            SkillScriptLoadHelper.loadScripts(skillPackage);
            importedCount++;
        }

        return new SkillImportResult(importedCount, skippedCount, allSkillNames.size());
    }
}
