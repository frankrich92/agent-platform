package com.htam.agent.runtime.agentscope.tool;

import com.htam.agent.common.entity.AgentCodeExecution;
import com.htam.agent.common.entity.AgentDefinition;
import com.htam.agent.common.entity.AgentSubAgent;
import com.htam.agent.common.entity.AgentTool;
import com.htam.agent.common.entity.CodeExecutionConfig;
import com.htam.agent.common.entity.ToolConfig;
import com.htam.agent.common.enums.ToolType;
import com.htam.agent.repo.agent.AgentCodeExecutionRepository;
import com.htam.agent.repo.agent.AgentDefinitionRepository;
import com.htam.agent.repo.agent.AgentSubAgentRepository;
import com.htam.agent.repo.agent.CodeExecutionConfigRepository;
import com.htam.agent.repo.capability.AgentToolRepository;
import com.htam.agent.repo.capability.ToolConfigRepository;
import com.htam.agent.runtime.agentscope.agent.A2aAgentHelper;
import com.htam.agent.runtime.agentscope.agent.ReActAgentHelper;
import com.htam.agent.runtime.agentscope.agui.AgentContext;
import com.htam.agent.runtime.agentscope.hook.builtins.IConfirmationHook;
import com.htam.agent.runtime.agentscope.mcp.McpClientFactory;
import com.htam.agent.runtime.agentscope.tool.dynamices.DynamicAgentTool;
import com.htam.agent.runtime.agentscope.workspace.tool.SearchReplaceFileTool;
import io.agentscope.core.model.ExecutionConfig;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.ToolkitConfig;
import io.agentscope.core.tool.subagent.SubAgentConfig;
import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * 描述：工具工厂
 *
 * @author huxuehao
 */
@Slf4j
@Component
public class ToolkitFactory {
    private final ToolConfigRepository toolConfigRepository;
    private final AgentToolRepository agentToolRepository;
    private final AgentSubAgentRepository agentSubAgentRepository;
    private final ReActAgentHelper reActAgentHelper;
    private final A2aAgentHelper a2aAgentHelper;
    private final McpClientFactory mcpClientFactory;
    private final AgentDefinitionRepository agentDefinitionRepository;
    private final AgentCodeExecutionRepository agentCodeExecutionRepository;
    private final CodeExecutionConfigRepository codeExecutionConfigRepository;

    public ToolkitFactory(ToolConfigRepository toolConfigRepository,
                          AgentToolRepository agentToolRepository,
                          AgentSubAgentRepository agentSubAgentRepository,
                          @Lazy
                          ReActAgentHelper reActAgentHelper,
                          @Lazy
                          A2aAgentHelper a2aAgentHelper,
                          McpClientFactory mcpClientFactory,
                          AgentCodeExecutionRepository agentCodeExecutionRepository,
                          CodeExecutionConfigRepository codeExecutionConfigRepository,
                          AgentDefinitionRepository agentDefinitionRepository) {
        this.toolConfigRepository = toolConfigRepository;
        this.agentToolRepository = agentToolRepository;
        this.agentSubAgentRepository = agentSubAgentRepository;
        this.reActAgentHelper = reActAgentHelper;
        this.a2aAgentHelper = a2aAgentHelper;
        this.mcpClientFactory = mcpClientFactory;
        this.agentCodeExecutionRepository = agentCodeExecutionRepository;
        this.codeExecutionConfigRepository = codeExecutionConfigRepository;
        this.agentDefinitionRepository = agentDefinitionRepository;
    }

    public Toolkit getToolkit(AgentDefinition agentDefinition) {
        List<Long> toolIds = getAgentToolIds(agentDefinition.getId());
        Toolkit toolkit = getToolkit(toolIds);

        if (!toolIds.isEmpty()) {
            // 注册工具
            toolConfigRepository.listByIds(toolIds)
                    .stream()
                    .filter(ToolConfig::getEnabled)
                    .forEach(toolConfig -> {
                        // 内置工具注册
                        if (toolConfig.getToolType() == ToolType.BUILTIN) {
                            toolkit.registerTool(ToolsRegister.getTool(toolConfig.getClassPath()));
                        } else {
                            // 动态工具注册
                            toolkit.registerTool(new DynamicAgentTool(toolConfig));
                        }

                        if (needConfirm(toolConfig)) {
                            IConfirmationHook.setNeedConfirmTool(toolConfig.getToolId());
                        } else {
                            IConfirmationHook.removeNeedConfirmTool(toolConfig.getToolId());
                        }
                    });
        }

        // 注册文件搜索替换工具
        AgentCodeExecution agentCodeExecution = agentCodeExecutionRepository.getByAgentId(agentDefinition.getId());
        Long codeExecutionId = agentCodeExecution == null ? null : agentCodeExecution.getCodeExecutionId();
        if (codeExecutionId != null) {
            CodeExecutionConfig config = codeExecutionConfigRepository.getById(codeExecutionId);
            if (config != null && config.getEnabled() && config.getEnableWrite()) {
                toolkit.registerTool(new SearchReplaceFileTool());
            }
        }

        // 此处仅注册缓存的 MCP 工具模式，真正的 MCP 连接会在调用时打开。
        mcpClientFactory.getLazyMcpTools(agentDefinition).forEach(toolkit::registerAgentTool);

        // 注册 Agent as Tool
        List<Long> subAgentIds = agentSubAgentRepository.listByParentAgentId(agentDefinition.getId())
                .stream()
                .map(AgentSubAgent::getSubAgentId)
                .toList();
        if (!subAgentIds.isEmpty()) {
            registerSubAgents(toolkit, subAgentIds);
        }

        return toolkit;
    }

    public Toolkit getToolkit(List<Long> toolIds) {
        Toolkit toolkit = new Toolkit(
                ToolkitConfig.builder()
                        // 禁止并行执行多个工具
                        .parallel(false)
                        // 禁止删除工具
                        .allowToolDeletion(false)
                        // 设置工具执行超时时间为 60 秒
                        .executionConfig(ExecutionConfig.builder().timeout(Duration.ofSeconds(60)).build())
                        .build());
        if (!toolIds.isEmpty()) {
            // 获取是否开启记忆
            Boolean isMemoryActive = AgentContext.getIfExists().map(AgentContext::isMemoryActive).orElse(false);
            // 注册工具
            toolConfigRepository.listByIds(toolIds)
                    .stream()
                    .filter(ToolConfig::getEnabled)
                    .forEach(toolConfig -> {
                        // 内置工具注册
                        if (toolConfig.getToolType() == ToolType.BUILTIN) {
                            toolkit.registerTool(ToolsRegister.getTool(toolConfig.getClassPath()));
                        } else {
                            // 动态工具注册
                            toolkit.registerTool(new DynamicAgentTool(toolConfig));
                        }

                        if (needConfirm(toolConfig) && isMemoryActive) {
                            IConfirmationHook.setNeedConfirmTool(toolConfig.getToolId());
                        } else {
                            IConfirmationHook.removeNeedConfirmTool(toolConfig.getToolId());
                        }
                    });
        }
        return toolkit;
    }

    static boolean needConfirm(ToolConfig toolConfig) {
        return toolConfig != null && Boolean.TRUE.equals(toolConfig.getNeedConfirm());
    }

    private List<Long> getAgentToolIds(Long agentDefinitionId) {
        return agentToolRepository.listByAgentDefinitionId(agentDefinitionId)
                .stream()
                .map(AgentTool::getToolId)
                .toList();
    }

    private void registerSubAgents(Toolkit toolkit, List<Long> subAgentIds) {
        for (Long subAgentId : subAgentIds) {
            AgentDefinition definition = agentDefinitionRepository.getById(subAgentId);

            if (definition == null || !definition.getEnabled()) {
                continue;
            }

            try {
                // Agent as Tool
                switch (definition.getAgentType()) {
                    case CUSTOM:
                        toolkit.registration()
                                .subAgent(() -> reActAgentHelper.getReActAgent(definition),
                                        createSubAgentConfig(definition))
                                .apply();
                        break;
                    case A2A:
                        toolkit.registration()
                                .subAgent(() -> a2aAgentHelper.getA2aAgent(definition),
                                        createSubAgentConfig(definition))
                                .apply();
                        break;
                    default:
                        break;
                }
                log.debug("Register sub agent: {}", subAgentId);
            } catch (Exception e) {
                log.error("Registration of sub agent failed: {}", subAgentId, e);
            }
        }
    }

    private SubAgentConfig createSubAgentConfig(AgentDefinition definition) {
        return SubAgentConfig.builder()
                .toolName(definition.getAgentCode().toLowerCase())
                .description(definition.getDescription() != null ?
                        definition.getDescription() : definition.getName())
                .forwardEvents(true)
                .build();
    }
}
