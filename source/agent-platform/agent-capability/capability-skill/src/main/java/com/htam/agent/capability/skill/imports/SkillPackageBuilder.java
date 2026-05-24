package com.htam.agent.capability.skill.imports;

import com.htam.agent.common.entity.SkillPackage;
import com.htam.agent.common.util.JsonUtils;

import java.util.ArrayList;
import java.util.Map;

/**
 * 描述：技能包构建器，负责将导入源解析结果转换为 SkillPackage 实体
 *
 * @author huxuehao
 **/
public class SkillPackageBuilder {

    /**
     * 基于导入源解析结果构建 SkillPackage 实体
     *
     * @param importedSkill 导入源解析结果
     * @param category   技能分类
     * @return SkillPackage 实体
     */
    public static SkillPackage build(ImportedSkill importedSkill, String category) {
        SkillPackage skillPackage = new SkillPackage();
        skillPackage.setCategory(category);
        skillPackage.setName(importedSkill.name());
        skillPackage.setDescription(importedSkill.description());
        skillPackage.setSkillContent(importedSkill.skillContent());

        ArrayList<SkillPackageItem> examples = new ArrayList<>();
        ArrayList<SkillPackageItem> references = new ArrayList<>();
        ArrayList<SkillPackageItem> scripts = new ArrayList<>();

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
        });

        skillPackage.setExamples(JsonUtils.valueToTree(examples));
        skillPackage.setReferences(JsonUtils.valueToTree(references));
        skillPackage.setScripts(JsonUtils.valueToTree(scripts));

        return skillPackage;
    }
}
