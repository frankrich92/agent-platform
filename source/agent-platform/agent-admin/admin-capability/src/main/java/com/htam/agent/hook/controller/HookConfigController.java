package com.htam.agent.hook.controller;

import com.htam.agent.common.config.auth.RoleNeed;
import com.htam.agent.common.dto.HookConfigDTO;
import com.htam.agent.common.entity.HookConfig;
import com.htam.agent.common.enums.Role;
import com.htam.agent.common.mp.support.MP;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.common.r.R;
import com.htam.agent.common.util.BeanUtils;
import com.htam.agent.common.vo.HookConfigVO;
import com.htam.agent.hook.service.HookConfigService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Hook配置Controller
 *
 * @author huxuehao
 */
@RestController
@RequestMapping("/hook-config")
@RequiredArgsConstructor
public class HookConfigController {

    private final HookConfigService hookConfigService;

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public R<IPage<HookConfigVO>> page(PageParams pageParams, HookConfigDTO query) {
        IPage<HookConfig> page = hookConfigService.page(MP.getPage(pageParams), MP.getQueryWrapper(query));
        return R.data(BeanUtils.copyPage(page, HookConfigVO.class));
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public R<HookConfigVO> detail(@PathVariable("id") Long id) {
        HookConfig entity = hookConfigService.getById(id);

        HookConfigVO vo = BeanUtils.copy(entity, HookConfigVO.class);
        vo.setUsed(hookConfigService.usedWithAgent(List.of(id)));

        return R.data(vo);
    }

    /**
     * 新增
     */
    @PostMapping
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<Boolean> save(@RequestBody HookConfig entity) {
        return R.data(hookConfigService.save(entity));
    }

    /**
     * 修改
     */
    @PutMapping
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<Boolean> update(@RequestBody HookConfig entity) {
        return R.data(hookConfigService.doUpdate(entity));
    }

    /**
     * 删除
     */
    @DeleteMapping
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.data(hookConfigService.deleteByIds(ids));
    }

    /**
     * 被哪些Agent使用
     */
    @PostMapping("used-with-agent")
    public R<List<Object>> usedWithAgent(@RequestBody List<Long> ids) {
        return R.data(hookConfigService.usedWithAgent(ids));
    }
}
