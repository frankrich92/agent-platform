package com.htam.agent.tool.controller;

import com.htam.agent.common.config.auth.RoleNeed;
import com.htam.agent.common.dto.ToolDTO;
import com.htam.agent.common.entity.ToolConfig;
import com.htam.agent.common.enums.Role;
import com.htam.agent.common.enums.ToolType;
import com.htam.agent.common.mp.support.MP;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.common.r.R;
import com.htam.agent.common.util.BeanUtils;
import com.htam.agent.common.vo.ToolVO;
import com.htam.agent.tool.service.ToolService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工具Controller
 *
 * @author huxuehao
 */
@RestController
@RequestMapping("/tool")
@RequiredArgsConstructor
public class ToolController {

    private final ToolService toolService;

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public R<IPage<ToolVO>> page(PageParams pageParams, ToolDTO query) {
        IPage<ToolConfig> page = toolService.page(MP.getPage(pageParams), MP.getQueryWrapper(query));
        return R.data(BeanUtils.copyPage(page, ToolVO.class));
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public R<ToolVO> detail(@PathVariable("id") Long id) {
        ToolConfig entity = toolService.getById(id);

        ToolVO vo = BeanUtils.copy(entity, ToolVO.class);
        vo.setUsed(toolService.usedWithAgent(List.of(id)));

        return R.data(vo);
    }

    /**
     * 新增
     */
    @PostMapping
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<Boolean> save(@RequestBody ToolConfig entity) {
        entity.setToolType(ToolType.CUSTOM);
        return R.data(toolService.save(entity));
    }

    /**
     * 修改
     */
    @PutMapping
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<Boolean> update(@RequestBody ToolConfig entity) {
        return R.data(toolService.doUpdate(entity));
    }

    /**
     * 删除
     */
    @DeleteMapping
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.data(toolService.deleteTools(ids));
    }

    /**
     * 被哪些Agent使用
     */
    @PostMapping("used-with-agent")
    public R<List<Object>> usedWithAgent(@RequestBody List<Long> ids) {
        return R.data(toolService.usedWithAgent(ids));
    }

    /**
     * 获取所有分类
     */
    @GetMapping("/get/categories")
    public R<List<String>> listCategories() {
        return R.data(toolService.listCategories());
    }
}
