package com.htam.agent.runtime.agentscope.skill;

import com.htam.agent.common.consts.SysConst;
import com.htam.agent.common.entity.AgentCodeExecution;
import com.htam.agent.common.entity.AgentDefinition;
import com.htam.agent.common.entity.AgentSkillPackage;
import com.htam.agent.common.entity.CodeExecutionConfig;
import com.htam.agent.common.entity.SkillPackage;
import com.htam.agent.common.entity.SkillTool;
import com.htam.agent.common.key.SkillExampleKey;
import com.htam.agent.common.key.SkillReferencesKey;
import com.htam.agent.common.key.SkillScriptKey;
import com.htam.agent.runtime.agentscope.agui.AgentContext;
import com.htam.agent.runtime.agentscope.tool.ToolkitFactory;
import com.htam.agent.runtime.agentscope.workspace.skills.SearchReplaceSkill;
import com.htam.agent.runtime.agentscope.workspace.skills.WorkspaceSkill;
import com.fasterxml.jackson.databind.JsonNode;
import com.htam.agent.repo.agent.AgentCodeExecutionRepository;
import com.htam.agent.repo.agent.CodeExecutionConfigRepository;
import com.htam.agent.repo.capability.AgentSkillPackageRepository;
import com.htam.agent.repo.capability.SkillPackageRepository;
import com.htam.agent.repo.capability.SkillToolRepository;
import com.htam.agent.runtime.core.RuntimeInteractionRecorder;
import io.agentscope.core.skill.AgentSkill;
import io.agentscope.core.skill.SkillBox;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.coding.ShellCommandTool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 描述：skill 构造器
 *
 * @author huxuehao
 **/
@Component
@RequiredArgsConstructor
public class SkillBoxFactory {
    private final ToolkitFactory toolkitFactory;
    private final SkillToolRepository skillToolRepository;
    private final SkillPackageRepository skillPackageRepository;
    private final AgentSkillPackageRepository agentSkillPackageRepository;
    private final AgentCodeExecutionRepository agentCodeExecutionRepository;
    private final CodeExecutionConfigRepository codeExecutionConfigRepository;

    /**
     * 获取SkillBox
     *
     * @param agentDefinition 智能体定义
     * @return SkillBox
     */
    public SkillBox getSkillBox(AgentDefinition agentDefinition) {
        SkillBox skillBox = new SkillBox(new Toolkit());

        // 注册技能包
        List<Long> skillPackageIds = agentSkillPackageRepository.listByAgentDefinitionId(agentDefinition.getId())
                .stream()
                .map(AgentSkillPackage::getSkillPackageId)
                .toList();
        if (skillPackageIds.isEmpty()) {
            return skillBox;
        }

        registerSkills(skillBox, skillPackageIds);

        return skillBox;
    }

    /**
     * 注册技能包到SkillBox
     *
     * @param skillBox SkillBox
     * @param skillPackageIds 技能包ID列表
     */
    private void registerSkills(SkillBox skillBox, List<Long> skillPackageIds) {
        List<SkillPackage> skillPackages = skillPackageRepository.listByIds(skillPackageIds);

        skillPackages.stream()
                .filter(SkillPackage::getEnabled)
                .forEach(skillPackage -> registerSkill(skillBox, skillPackage));
    }

    /**
     * 注册单个技能包
     *
     * @param skillBox SkillBox
     * @param skillPackage 技能包
     */
    private void registerSkill(SkillBox skillBox, SkillPackage skillPackage) {
        AgentSkill.Builder skillBuilder = AgentSkill.builder()
                .name(skillPackage.getName())
                .description(skillPackage.getDescription())
                .skillContent(skillPackage.getSkillContent());
        RuntimeInteractionRecorder.recordSkillLoad(skillPackage.getName(), "skill:" + skillPackage.getId());

        // 添加资源引用
        addResources(skillBuilder, skillPackage.getReferences(), SkillReferencesKey.prefix,
                SkillReferencesKey.name, SkillReferencesKey.content);

        // 添加示例
        addResources(skillBuilder, skillPackage.getExamples(), SkillExampleKey.prefix,
                SkillExampleKey.name, SkillExampleKey.content);

        // 添加脚本
        addResources(skillBuilder, skillPackage.getScripts(), SkillScriptKey.prefix,
                SkillScriptKey.name, SkillScriptKey.content);

        // 获取关联的工具
        Toolkit toolkit = toolkitFactory.getToolkit(getSkillToolIds(skillPackage.getId()));

        skillBox.registration().skill(skillBuilder.build()).tool(toolkit).apply();
    }

    /**
     * 添加资源到技能构建器
     *
     * @param skillBuilder 技能构建器
     * @param resources 资源JSON节点
     * @param prefixKey 前缀键
     * @param nameKey 名称键
     * @param contentKey 内容键
     */
    private void addResources(AgentSkill.Builder skillBuilder, JsonNode resources,
                              String prefixKey, String nameKey, String contentKey) {
        if (resources == null || resources.isEmpty()) {
            return;
        }
        resources.forEach(resource -> {
            String prefix = resource.get(prefixKey).asText();
            String name = resource.get(nameKey).asText();
            String content = resource.get(contentKey).asText();
            skillBuilder.addResource(String.format("%s/%s", prefix, name), content);
        });
    }

    /**
     * 配置代码执行环境
     *
     * @param skillBox SkillBox
     * @param agentDefinitionId 智能体定义ID
     */
    public void configureCodeExecution(SkillBox skillBox, Long agentDefinitionId) {
        if (skillBox == null) {
            return;
        }
        // 获取代码执行配置
        CodeExecutionConfig config = getCodeExecutionConfig(agentDefinitionId);
        if (config == null) {
            return;
        }

        // 配置工作空间专属skill
        skillBox.registerSkill(WorkspaceSkill.getAgentSkill());

        // 设置自动上传
        skillBox.setAutoUploadSkill(false);

        // 配置代码执行环境
        SkillBox.CodeExecutionBuilder codeExecutionBuilder = skillBox.codeExecution();

        // 设置工作目录
        codeExecutionBuilder.workDir(SysConst.WORKSPACE_PATH + "/" + AgentContext.get().getThreadId());

        // 配置Shell命令工具
        if (Boolean.TRUE.equals(config.getEnableShell())) {
            Set<String> allowedCommands = parseAllowedCommands(config.getCommand());
            codeExecutionBuilder.withShell(new ShellCommandTool(null, allowedCommands, null));
        }

        // 配置文件读写工具
        if (Boolean.TRUE.equals(config.getEnableRead())) {
            codeExecutionBuilder.withRead();
        }
        if (Boolean.TRUE.equals(config.getEnableWrite())) {
            codeExecutionBuilder.withWrite();
            // 配置工作空间专属skill
            skillBox.registerSkill(SearchReplaceSkill.getAgentSkill());
        }

        codeExecutionBuilder.enable();
    }

    /**
     * 获取代码执行配置
     *
     * @param agentDefinitionId 智能体定义ID
     * @return 代码执行配置
     */
    private CodeExecutionConfig getCodeExecutionConfig(Long agentDefinitionId) {
        AgentCodeExecution agentCodeExecution = agentCodeExecutionRepository.getByAgentId(agentDefinitionId);
        Long codeExecutionId = agentCodeExecution == null ? null : agentCodeExecution.getCodeExecutionId();
        if (codeExecutionId == null) {
            return null;
        }
        return codeExecutionConfigRepository.getById(codeExecutionId);
    }

    private List<Long> getSkillToolIds(Long skillId) {
        return skillToolRepository.listBySkillId(skillId)
                .stream()
                .map(SkillTool::getToolId)
                .toList();
    }

    /**
     * 解析允许执行的命令集合
     *
     * @param commandJson 命令JSON节点
     * @return 允许执行的命令集合
     */
    private Set<String> parseAllowedCommands(JsonNode commandJson) {
        Set<String> commands = new HashSet<>();
        if (commandJson == null || commandJson.isEmpty()) {
            return commands;
        }
        if (commandJson.isArray()) {
            commandJson.forEach(node -> commands.add(node.asText()));
        }
        return commands;
    }
}
