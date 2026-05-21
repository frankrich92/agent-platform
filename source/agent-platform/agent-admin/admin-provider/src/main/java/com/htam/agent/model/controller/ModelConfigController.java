package com.htam.agent.model.controller;

import com.htam.agent.common.config.auth.RoleNeed;
import com.htam.agent.common.dto.ModelConfigDTO;
import com.htam.agent.common.entity.ModelConfig;
import com.htam.agent.common.enums.Role;
import com.htam.agent.common.mp.support.MP;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.common.r.R;
import com.htam.agent.common.util.BeanUtils;
import com.htam.agent.common.vo.ModelConfigVO;
import com.htam.agent.model.service.ModelConfigService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 模型配置Controller
 *
 * @author huxuehao
 */
@RestController
@RequestMapping("/model/config")
@RequiredArgsConstructor
public class ModelConfigController {

    private final ModelConfigService modelConfigService;

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public R<IPage<ModelConfigVO>> page(PageParams pageParams, ModelConfigDTO query) {
        IPage<ModelConfig> page = modelConfigService.page(MP.getPage(pageParams), MP.getQueryWrapper(query));
        return R.data(BeanUtils.copyPage(page, ModelConfigVO.class));
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public R<ModelConfigVO> detail(@PathVariable("id") Long id) {
        ModelConfig entity = modelConfigService.getById(id);

        ModelConfigVO vo = BeanUtils.copy(entity, ModelConfigVO.class);
        vo.setUsed(modelConfigService.usedWithAgent(List.of(id)));

        return R.data(vo);
    }

    /**
     * 新增
     */
    @PostMapping
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<Boolean> save(@RequestBody ModelConfig entity) {
        return R.data(modelConfigService.save(entity));
    }

    /**
     * 修改
     */
    @PutMapping
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<Boolean> update(@RequestBody ModelConfig entity) {
        return R.data(modelConfigService.doUpdate(entity));
    }

    /**
     * 删除
     */
    @DeleteMapping
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.data(modelConfigService.deleteByIds(ids));
    }

    /**
     * 被哪些Agent使用
     */
    @PostMapping("used-with-agent")
    public R<List<Object>> usedWithAgent(@RequestBody List<Long> ids) {
        return R.data(modelConfigService.usedWithAgent(ids));
    }
}
