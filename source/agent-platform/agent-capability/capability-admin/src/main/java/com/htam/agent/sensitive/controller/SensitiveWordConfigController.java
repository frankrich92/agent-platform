package com.htam.agent.sensitive.controller;

import com.htam.agent.common.config.auth.RoleNeed;
import com.htam.agent.common.dto.SensitiveWordConfigDTO;
import com.htam.agent.common.entity.SensitiveWordConfig;
import com.htam.agent.common.enums.Role;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.common.r.R;
import com.htam.agent.common.util.BeanUtils;
import com.htam.agent.common.vo.SensitiveWordConfigVO;
import com.htam.agent.sensitive.service.SensitiveWordConfigService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 敏感词配置Controller
 *
 * @author huxuehao
 */
@RestController
@RequestMapping("/sensitive/config")
@RequiredArgsConstructor
public class SensitiveWordConfigController {

    private final SensitiveWordConfigService sensitiveWordConfigService;

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public R<IPage<SensitiveWordConfigVO>> page(PageParams pageParams, SensitiveWordConfigDTO query) {
        IPage<SensitiveWordConfig> page = sensitiveWordConfigService.page(pageParams, query);
        return R.data(BeanUtils.copyPage(page, SensitiveWordConfigVO.class));
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public R<SensitiveWordConfigVO> detail(@PathVariable("id") Long id) {
        SensitiveWordConfig entity = sensitiveWordConfigService.getById(id);

        SensitiveWordConfigVO vo = BeanUtils.copy(entity, SensitiveWordConfigVO.class);
        vo.setUsed(sensitiveWordConfigService.usedWithAgent(List.of(id)));

        return R.data(vo);
    }

    /**
     * 新增
     */
    @PostMapping
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<Boolean> save(@RequestBody SensitiveWordConfig entity) {
        return R.data(sensitiveWordConfigService.save(entity));
    }

    /**
     * 修改
     */
    @PutMapping
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<Boolean> update(@RequestBody SensitiveWordConfig entity) {
        return R.data(sensitiveWordConfigService.doUpdate(entity));
    }

    /**
     * 删除
     */
    @DeleteMapping
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.data(sensitiveWordConfigService.deleteByIds(ids));
    }

    /**
     * 被哪些Agent使用
     */
    @PostMapping("used-with-agent")
    public R<List<Object>> usedWithAgent(@RequestBody List<Long> ids) {
        return R.data(sensitiveWordConfigService.usedWithAgent(ids));
    }

    /**
     * 获取所有分类
     */
    @GetMapping("/get/categories")
    public R<List<String>> listCategories() {
        return R.data(sensitiveWordConfigService.listCategories());
    }
}
