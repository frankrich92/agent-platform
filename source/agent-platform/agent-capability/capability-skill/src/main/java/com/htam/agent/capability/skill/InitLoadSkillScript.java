package com.htam.agent.capability.skill;

import com.htam.agent.capability.skill.service.SkillFileService;
import com.htam.agent.common.entity.SkillFile;
import com.htam.agent.common.entity.SkillPackage;
import com.htam.agent.common.enums.SkillFileType;
import com.htam.agent.capability.skill.service.SkillPackageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 描述：InitLoadSkillScript
 *
 * @author huxuehao
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class InitLoadSkillScript implements ApplicationRunner {
    private final SkillPackageService skillPackageService;
    private final SkillFileService skillFileService;

    @Override
    public void run(ApplicationArguments args) {
        List<SkillPackage> list = skillPackageService.listAll();
        for (SkillPackage skillPackage : list) {
            List<SkillFile> files = skillFileService.listBySkillId(skillPackage.getId());
            for (SkillFile file : files) {
                SkillFileSystemService.writeFile(skillPackage.getName(), file.getFilePath(), file.getContent());
            }
            syncMissingFiles(skillPackage, SkillFileSystemService.scanSkillTree(skillPackage.getName()));
            SkillScriptLoadHelper.loadScripts(skillPackage);
            log.info("已同步技能包 {} 的文件到本地", skillPackage.getName());
        }
    }

    private void syncMissingFiles(SkillPackage skillPackage, List<SkillFileSystemService.FileTreeNode> nodes) {
        for (SkillFileSystemService.FileTreeNode node : nodes) {
            if (node.isDirectory()) {
                syncMissingFiles(skillPackage, node.getChildren());
                continue;
            }
            String relPath = node.getPath().replace('\\', '/');
            if (!SkillFileSystemService.shouldPersistToDb(relPath)
                    || skillFileService.getBySkillIdAndPath(skillPackage.getId(), relPath) != null) {
                continue;
            }
            String content = SkillFileSystemService.readFileContent(skillPackage.getName(), relPath);
            SkillFile sf = new SkillFile();
            sf.setSkillId(skillPackage.getId());
            sf.setFileType(SkillFileSystemService.resolveFileType(relPath));
            sf.setFileName(node.getName());
            sf.setFilePath(relPath);
            sf.setContent(content == null ? "" : content);
            sf.setSort(0);
            skillFileService.save(sf);
            if (sf.getFileType() == SkillFileType.SKILL_MD && skillPackage.getSkillContent() == null) {
                skillPackage.setSkillContent(sf.getContent());
                skillPackageService.updateById(skillPackage);
            }
            log.info("补录技能包文件到 DB: skillName={}, path={}", skillPackage.getName(), relPath);
        }
    }
}
