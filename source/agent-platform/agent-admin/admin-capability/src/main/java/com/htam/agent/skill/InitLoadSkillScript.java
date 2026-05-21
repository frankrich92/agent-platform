package com.htam.agent.skill;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htam.agent.common.entity.SkillPackage;
import com.htam.agent.skill.service.SkillPackageService;
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

    @Override
    public void run(ApplicationArguments args) {
        List<SkillPackage> list = skillPackageService.list(new LambdaQueryWrapper<SkillPackage>().ne(SkillPackage::getScripts, "[]"));
        for (SkillPackage skillPackage : list) {
            SkillScriptLoadHelper.loadScripts(skillPackage);
            log.info("已经重新导入{}的脚本到本地", skillPackage.getName());
        }
    }
}
