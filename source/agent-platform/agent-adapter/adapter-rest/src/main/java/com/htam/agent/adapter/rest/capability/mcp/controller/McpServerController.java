package com.htam.agent.adapter.rest.capability.mcp.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.htam.agent.common.config.auth.RoleNeed;
import com.htam.agent.common.dto.McpServerDTO;
import com.htam.agent.common.dto.McpToolEnabledDTO;
import com.htam.agent.common.entity.McpServer;
import com.htam.agent.common.enums.Role;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.common.r.R;
import com.htam.agent.common.util.BeanUtils;
import com.htam.agent.common.vo.McpServerVO;
import com.htam.agent.common.vo.McpToolVO;
import com.htam.agent.capability.mcp.service.McpServerService;
import com.htam.agent.capability.mcp.service.McpToolService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MCP 服务配置 Controller
 *
 * @author huxuehao
 */
@RestController
@RequestMapping("/mcp/server")
@RequiredArgsConstructor
public class McpServerController {

    private final McpServerService mcpServerService;
    private final McpToolService mcpToolService;

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public R<IPage<McpServerVO>> page(PageParams pageParams, McpServerDTO query) {
        IPage<McpServer> page = mcpServerService.page(pageParams, query);
        IPage<McpServerVO> pageVo = BeanUtils.copyPage(page, McpServerVO.class);
        fillAvailableToolCount(pageVo.getRecords());
        return R.data(pageVo);
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public R<McpServerVO> detail(@PathVariable("id") Long id) {
        return R.data(toVo(mcpServerService.getById(id)));
    }

    /**
     * 新增
     */
    @PostMapping
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<McpServerVO> save(@RequestBody McpServer entity) {
        mcpServerService.save(entity);
        return R.data(toVo(mcpServerService.getById(entity.getId())));
    }

    /**
     * 修改
     */
    @PutMapping
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<McpServerVO> update(@RequestBody McpServer entity) {
        return R.data(toVo(mcpServerService.doUpdate(entity)));
    }

    /**
     * 激活
     */
    @PostMapping("/{id}/activate")
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<McpServerVO> activate(@PathVariable("id") Long id) {
        return R.data(toVo(mcpServerService.activate(id)));
    }

    /**
     * 同步工具目录
     */
    @PostMapping("/{id}/sync-tools")
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<McpServerVO> syncTools(@PathVariable("id") Long id) {
        return R.data(toVo(mcpServerService.syncTools(id)));
    }

    /**
     * 工具列表
     */
    @GetMapping("/{id}/tools")
    public R<List<McpToolVO>> listTools(@PathVariable("id") Long id) {
        return R.data(mcpServerService.listTools(id));
    }

    /**
     * 批量切换工具全局可用状态
     */
    @PutMapping("/{id}/tools/global-enabled")
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<McpServerVO> updateToolGlobalEnabled(@PathVariable("id") Long id,
                                                  @RequestBody McpToolEnabledDTO dto) {
        return R.data(toVo(mcpServerService.updateToolGlobalEnabled(id, dto)));
    }

    /**
     * 删除
     */
    @DeleteMapping
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.data(mcpServerService.deleteByIds(ids));
    }

    /**
     * 被哪些 Agent 使用
     */
    @PostMapping("used-with-agent")
    public R<List<Object>> usedWithAgent(@RequestBody List<Long> ids) {
        return R.data(mcpServerService.usedWithAgent(ids));
    }

    private McpServerVO toVo(McpServer entity) {
        McpServerVO vo = BeanUtils.copy(entity, McpServerVO.class);
        vo.setUsed(mcpServerService.usedWithAgent(List.of(entity.getId())));
        fillAvailableToolCount(List.of(vo));
        return vo;
    }

    private void fillAvailableToolCount(List<McpServerVO> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        Map<Long, Integer> countMap = mcpToolService.countAvailableTools(items.stream()
                .map(item -> Long.valueOf(String.valueOf(item.getId())))
                .collect(Collectors.toList()));
        items.forEach(item -> item.setAvailableToolCount(countMap.getOrDefault(
                Long.valueOf(String.valueOf(item.getId())),
                0)));
    }
}
