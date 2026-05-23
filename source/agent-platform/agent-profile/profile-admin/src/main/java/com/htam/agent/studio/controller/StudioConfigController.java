package com.htam.agent.studio.controller;

import com.htam.agent.common.config.auth.RoleNeed;
import com.htam.agent.common.entity.StudioConfig;
import com.htam.agent.common.enums.Role;
import com.htam.agent.common.r.R;
import com.htam.agent.studio.service.StudioConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 描述：StudioConfigController
 *
 * @author huxuehao
 **/
@RestController
@RequestMapping("/studio")
@RequiredArgsConstructor
public class StudioConfigController {
    private final StudioConfigService studioConfigService;

    /**
     * 分页查询
     */
    @GetMapping("/list")
    public R<List<StudioConfig>> page() {
        return R.data(studioConfigService.list());
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public R<StudioConfig> detail(@PathVariable("id") Long id) {
        return R.data(studioConfigService.getById(id));
    }

    /**
     * 新增
     */
    @PostMapping
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<Boolean> save(@RequestBody StudioConfig entity) {
        return R.data(studioConfigService.save(entity));
    }

    /**
     * 修改
     */
    @PutMapping
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<Boolean> update(@RequestBody StudioConfig entity) {
        return R.data(studioConfigService.updateById(entity));
    }

    /**
     * 删除
     */
    @DeleteMapping
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.data(studioConfigService.removeByIds(ids));
    }

    /**
     * 被哪些Agent使用
     */
    @PostMapping("used-with-agent")
    public R<List<Object>> usedWithAgent(@RequestBody List<Long> ids) {
        return R.data(studioConfigService.usedWithAgent(ids));
    }
}
