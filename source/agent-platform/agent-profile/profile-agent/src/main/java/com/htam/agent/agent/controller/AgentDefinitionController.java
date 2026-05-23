package com.htam.agent.agent.controller;

import com.htam.agent.agent.service.AgentDefinitionService;
import com.htam.agent.cluster.core.MessagePublisher;
import com.htam.agent.common.config.auth.ChatKeyAccess;
import com.htam.agent.common.config.auth.RoleNeed;
import com.htam.agent.common.config.auth.SkAccess;
import com.htam.agent.common.consts.RedisChannelTopic;
import com.htam.agent.common.dto.AgentDefinitionDTO;
import com.htam.agent.common.entity.*;
import com.htam.agent.common.enums.Role;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.common.r.R;
import com.htam.agent.common.vo.AgentDefinitionVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 智能体定义Controller
 *
 * @author huxuehao
 */
@RestController
@RequestMapping("/agent/definition")
@RequiredArgsConstructor
public class AgentDefinitionController {

    private final AgentDefinitionService agentDefinitionService;
    private final MessagePublisher messagePublisher;

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public R<IPage<AgentDefinitionVO>> page(PageParams pageParams, AgentDefinitionDTO query) {
        return R.data(agentDefinitionService.pageAgentDefinitions(pageParams, query));
    }

    /**
     * 详情
     */
    @SkAccess
    @ChatKeyAccess
    @GetMapping("/{id}")
    public R<AgentDefinitionVO> detail(@PathVariable("id") Long id) {
        return R.data(agentDefinitionService.agentDefinitionDetail(id));
    }

    /**
     * 新增
     */
    @PostMapping
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<Boolean> save(@RequestBody AgentDefinitionVO vo) {
        agentDefinitionService.saveAgentDefinition(vo);
        messagePublisher.publish(RedisChannelTopic.AGENT_REREGISTER_CHANNEL, String.valueOf(vo.getId()));
        return R.data(true);
    }

    /**
     * 修改
     */
    @PutMapping
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<Boolean> update(@RequestBody AgentDefinitionVO vo) {
        return R.data(agentDefinitionService.updateAgentDefinition(vo));
    }

    /**
     * 删除
     */
    @DeleteMapping
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.data(agentDefinitionService.deleteAgentDefinition(ids));
    }

    /**
     * 被哪些Agent使用
     */
    @PostMapping("used-with-agent")
    public R<List<Object>> usedWithAgent(@RequestBody List<Long> ids) {
        return R.data(agentDefinitionService.usedWithAgent(ids));
    }

    /**
     * 获取所有Tag
     */
    @GetMapping("/get/tags")
    public R<List<String>> listTags() {
        return R.data(agentDefinitionService.listTags());
    }

    @SkAccess
    @ChatKeyAccess
    @GetMapping("/{id}/allow/file-type")
    public R<List<String>> allowFileType(@PathVariable("id") Long id) {
        return R.data(agentDefinitionService.allowFileType(id));
    }

    /**
     * 获取Agent启用的工具
     */
    @SkAccess
    @ChatKeyAccess
    @GetMapping("/{agentId}/enabled/tools")
    public R<List<ToolConfig>> getEnabledToolsOfAgent(@PathVariable("agentId") Long agentId) {
        return R.data(agentDefinitionService.getEnabledToolsOfAgent(agentId));
    }

    /**
     * 获取Agent启用的技能包
     */
    @SkAccess
    @ChatKeyAccess
    @GetMapping("/{agentId}/enabled/skills")
    public R<List<SkillPackage>> getEnabledSkillsOfAgent(@PathVariable("agentId") Long agentId) {
        return R.data(agentDefinitionService.getEnabledSkillsOfAgent(agentId));
    }
}
