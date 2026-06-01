package com.htam.agent.capability.skill.imports;

import com.htam.agent.capability.skill.SkillFileSystemService;
import com.htam.agent.common.entity.SkillFile;
import com.htam.agent.common.entity.SkillPackage;
import com.htam.agent.common.enums.SkillFileType;
import com.htam.agent.common.util.JsonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 描述：技能包构建器，负责将导入源解析结果转换为 SkillPackage 实体
 *
 * @author huxuehao
 **/
public class SkillPackageBuilder {

    public static class BuildResult {
        private final SkillPackage skillPackage;
        private final List<SkillFile> skillFiles;

        public BuildResult(SkillPackage skillPackage, List<SkillFile> skillFiles) {
            this.skillPackage = skillPackage;
            this.skillFiles = skillFiles;
        }

        public SkillPackage getSkillPackage() {
            return skillPackage;
        }

        public List<SkillFile> getSkillFiles() {
            return skillFiles;
        }
    }

    /**
     * 基于导入源解析结果构建 SkillPackage 实体
     *
     * @param importedSkill 导入源解析结果
     * @param category   技能分类
     * @return SkillPackage 实体
     */
    public static SkillPackage build(ImportedSkill importedSkill, String category) {
        return buildWithFiles(importedSkill, category).getSkillPackage();
    }

    public static BuildResult buildWithFiles(ImportedSkill importedSkill, String category) {
        SkillPackage skillPackage = new SkillPackage();
        skillPackage.setCategory(category);
        skillPackage.setName(importedSkill.name());
        skillPackage.setDescription(importedSkill.description());
        skillPackage.setSkillContent(importedSkill.skillContent());
        List<SkillFile> skillFiles = new ArrayList<>();

        ArrayList<SkillPackageItem> examples = new ArrayList<>();
        ArrayList<SkillPackageItem> references = new ArrayList<>();
        ArrayList<SkillPackageItem> scripts = new ArrayList<>();

        SkillFile skillMd = new SkillFile();
        skillMd.setFileType(SkillFileType.SKILL_MD);
        skillMd.setFileName("SKILL.md");
        skillMd.setFilePath("SKILL.md");
        skillMd.setContent(importedSkill.skillContent());
        skillMd.setSort(0);
        skillFiles.add(skillMd);

        Map<String, String> resources = importedSkill.resources();
        resources.forEach((path, content) -> {
            if (path.startsWith("examples/")) {
                examples.add(
                        SkillPackageItem
                                .builder()
                                .prefix("examples")
                                .name(path.substring("examples/".length()))
                                .content(content)
                                .build());
            } else if (path.startsWith("references/")) {
                references.add(
                        SkillPackageItem
                                .builder()
                                .prefix("references")
                                .name(path.substring("references/".length()))
                                .content(content)
                                .build());
            } else if (path.startsWith("scripts/")) {
                scripts.add(
                        SkillPackageItem
                                .builder()
                                .prefix("scripts")
                                .name(path.substring("scripts/".length()))
                                .content(content)
                                .build());
            }
            if (SkillFileSystemService.shouldPersistToDb(path)) {
                SkillFile sf = new SkillFile();
                sf.setFileType(SkillFileSystemService.resolveFileType(path));
                sf.setFileName(fileName(path));
                sf.setFilePath(path.replace('\\', '/'));
                sf.setContent(content);
                sf.setSort(skillFiles.size());
                skillFiles.add(sf);
            }
        });

        skillPackage.setExamples(JsonUtils.valueToTree(examples));
        skillPackage.setReferences(JsonUtils.valueToTree(references));
        skillPackage.setScripts(JsonUtils.valueToTree(scripts));

        return new BuildResult(skillPackage, skillFiles);
    }

    private static String fileName(String path) {
        String normalized = path == null ? "" : path.replace('\\', '/');
        int slash = normalized.lastIndexOf('/');
        return slash >= 0 ? normalized.substring(slash + 1) : normalized;
    }
}
