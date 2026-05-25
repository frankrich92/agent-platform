package com.htam.agent.adapter.rest.capability.skill.controller;

import com.htam.agent.common.config.auth.RoleNeed;
import com.htam.agent.common.dto.SkillPackageDTO;
import com.htam.agent.common.entity.SkillPackage;
import com.htam.agent.common.enums.Role;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.common.r.R;
import com.htam.agent.common.util.BeanUtils;
import com.htam.agent.common.vo.SkillImportResult;
import com.htam.agent.common.vo.SkillPackageVO;
import com.htam.agent.capability.skill.SkillScriptLoadHelper;
import com.htam.agent.capability.skill.imports.SkillImportService;
import com.htam.agent.capability.skill.imports.SkillInstaller;
import com.htam.agent.capability.skill.imports.config.GitImportConfig;
import com.htam.agent.capability.skill.imports.config.LocalImportConfig;
import com.htam.agent.capability.skill.service.SkillPackageService;
import com.htam.agent.capability.skill.service.SkillToolService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 技能包Controller
 *
 * @author huxuehao
 */
@RestController
@RequestMapping("/skill")
@RequiredArgsConstructor
public class SkillPackageController {

    private final SkillImportService skillImportService;
    private final SkillPackageService skillPackageService;
    private final SkillToolService skillToolService;

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public R<IPage<SkillPackageVO>> page(PageParams pageParams, SkillPackageDTO query) {
        IPage<SkillPackage> page = skillPackageService.page(pageParams, query);
        return R.data(BeanUtils.copyPage(page, SkillPackageVO.class));
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public R<SkillPackageVO> detail(@PathVariable("id") Long id) {
        SkillPackageVO vo = skillPackageService.getDetail(id);
        if (vo != null) {
            vo.setUsed(skillPackageService.usedWithAgent(List.of(id)));
        }
        return R.data(vo);
    }

    /**
     * 新增
     */
    @PostMapping
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<Boolean> save(@RequestBody SkillPackageVO vo) {
        SkillPackage entity = BeanUtils.copy(vo, SkillPackage.class);
        boolean save = skillPackageService.save(entity);
        // 保存技能与工具的关联
        if (vo.getTools() != null && !vo.getTools().isEmpty()) {
            skillToolService.saveSkillTool(entity.getId(), vo.getTools());
        }
        // 尝试装载脚本到本地
        SkillScriptLoadHelper.loadScripts(entity);
        return R.data(save);
    }

    /**
     * 修改
     */
    @PutMapping
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<Boolean> update(@RequestBody SkillPackageVO vo) {
        SkillPackage entity = BeanUtils.copy(vo, SkillPackage.class);
        boolean b = skillPackageService.doUpdate(entity);
        // 更新技能与工具的关联
        skillToolService.saveSkillTool(entity.getId(), vo.getTools());
        // 尝试装载脚本到本地
        if (entity.getScripts() == null || entity.getScripts().isNull() || entity.getScripts().isEmpty()) {
            SkillScriptLoadHelper.removeScripts(entity);
        } else {
            SkillScriptLoadHelper.loadScripts(entity);
        }

        return R.data(b);
    }

    /**
     * 删除
     */
    @DeleteMapping
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        List<SkillPackage> skillPackages = skillPackageService.listByIds(ids);
        for (SkillPackage skillPackage : skillPackages) {
            // 卸载技能包目录
            SkillInstaller.uninstall(skillPackage.getName());
        }
        return R.data(skillPackageService.deleteByIds(ids));
    }

    /**
     * 被哪些Agent使用
     */
    @PostMapping("used-with-agent")
    public R<List<Object>> usedWithAgent(@RequestBody List<Long> ids) {
        return R.data(skillPackageService.usedWithAgent(ids));
    }

    /**
     * 获取所有分类
     */
    @GetMapping("/get/categories")
    public R<List<String>> listCategories() {
        return R.data(skillPackageService.listCategories());
    }

    /**
     * 从git导入
     */
    @PostMapping("/import/git")
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<SkillImportResult> importFromGit(@RequestBody GitImportConfig config) {
        return R.data(skillImportService.importFromGit(config));
    }

    /**
     * 从本地导入
     */
    @PostMapping("/import/local")
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<SkillImportResult> importFromLocal(@RequestBody LocalImportConfig config) {
        return R.data(skillImportService.importFromLocal(config));
    }
    /**
     * 从压缩包导入
     *
     * @param file     技能包压缩包（zip 格式，解压后需包含 skills/ 目录）
     * @param category 技能分类
     * @param cover    是否覆盖已存在的同名技能
     */
    @PostMapping("/import/upload")
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<SkillImportResult> importFromUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") String category,
            @RequestParam("cover") boolean cover) throws IOException {

        return R.data(skillImportService.importFromUpload(file, category, cover));
    }
}
