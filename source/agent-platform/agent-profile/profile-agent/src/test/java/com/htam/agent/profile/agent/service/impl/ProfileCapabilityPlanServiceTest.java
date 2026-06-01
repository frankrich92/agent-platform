package com.htam.agent.profile.agent.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.htam.agent.capability.CapabilityItem;
import com.htam.agent.capability.CapabilityKind;
import com.htam.agent.capability.CapabilityPlan;
import com.htam.agent.capability.CapabilityRiskLevel;
import com.htam.agent.capability.CapabilityRiskPolicy;
import com.htam.agent.capability.knowledge.service.AgentKnowledgeBaseService;
import com.htam.agent.capability.knowledge.service.KnowledgeBaseConfigService;
import com.htam.agent.capability.mcp.service.AgentMcpServerService;
import com.htam.agent.capability.mcp.service.McpServerService;
import com.htam.agent.capability.mcp.service.McpToolService;
import com.htam.agent.capability.tool.hook.service.AgentHookService;
import com.htam.agent.capability.tool.hook.service.HookConfigService;
import com.htam.agent.common.dto.AgentDefinitionDTO;
import com.htam.agent.common.dto.HookConfigDTO;
import com.htam.agent.common.dto.KnowledgeBaseConfigDTO;
import com.htam.agent.common.dto.McpServerDTO;
import com.htam.agent.common.dto.McpToolEnabledDTO;
import com.htam.agent.common.entity.AgentDefinition;
import com.htam.agent.common.entity.AgentHook;
import com.htam.agent.common.entity.AgentKnowledgeBase;
import com.htam.agent.common.entity.AgentMcpServer;
import com.htam.agent.common.entity.HookConfig;
import com.htam.agent.common.entity.KnowledgeBaseConfig;
import com.htam.agent.common.entity.McpServer;
import com.htam.agent.common.entity.McpTool;
import com.htam.agent.common.entity.SkillPackage;
import com.htam.agent.common.entity.ToolConfig;
import com.htam.agent.common.enums.McpActivationStatus;
import com.htam.agent.common.enums.McpMode;
import com.htam.agent.common.enums.McpProtocol;
import com.htam.agent.common.enums.McpToolExposureMode;
import com.htam.agent.common.enums.ToolType;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.common.vo.AgentDefinitionVO;
import com.htam.agent.common.vo.AgentMcpBindingVO;
import com.htam.agent.common.vo.McpToolVO;
import com.htam.agent.common.wrapper.HookConfigWrapper;
import com.htam.agent.worker.code.service.AgentCodeExecutionService;
import com.htam.agent.profile.agent.service.AgentDefinitionService;
import com.htam.agent.profile.agent.service.AgentSubAgentService;
import com.htam.agent.worker.code.service.CodeExecutionConfigService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

class ProfileCapabilityPlanServiceTest {

    @Test
    void resolvePlanLoadsToolsMcpAndSkillsIntoCapabilityPlan() {
        RecordingAgentDefinitionService agentDefinitionService = new RecordingAgentDefinitionService();
        EmptyAgentMcpServerService agentMcpServerService = new EmptyAgentMcpServerService();
        RecordingMcpServerService mcpServerService = new RecordingMcpServerService();
        ProfileCapabilityPlanService service = new ProfileCapabilityPlanService(
                agentDefinitionService,
                List.of(
                        new ModelPolicyCapabilityContributor(),
                        new ToolCapabilityContributor(agentDefinitionService),
                        new SkillCapabilityContributor(agentDefinitionService),
                        new McpCapabilityContributor(agentMcpServerService, mcpServerService, new RecordingMcpToolService()),
                        new KnowledgeCapabilityContributor(new EmptyAgentKnowledgeBaseService(), new EmptyKnowledgeBaseConfigService()),
                        new HookCapabilityContributor(new EmptyAgentHookService(), new EmptyHookConfigService()),
                        new SubAgentCapabilityContributor(new EmptyAgentSubAgentService(), agentDefinitionService),
                        new WorkerCapabilityContributor(new EmptyAgentCodeExecutionService(), new EmptyCodeExecutionConfigService())));

        CapabilityPlan plan = service.resolvePlan(100L);

        assertEquals(1, agentDefinitionService.toolsCalled);
        assertEquals(1, agentDefinitionService.skillsCalled);
        assertEquals(1, agentMcpServerService.bindingsCalled);
        assertEquals(1, mcpServerService.getByIdCalled);

        CapabilityItem tool = find(plan, CapabilityKind.TOOL, "tool-search");
        assertEquals(CapabilityRiskPolicy.ALLOW, tool.riskPolicy());
        assertFalse(tool.readOnly());
        assertEquals("tool:builtin", tool.namespace());
        assertEquals("builtin", tool.attributes().get("category"));

        CapabilityItem skill = find(plan, CapabilityKind.SKILL, "skill-review");
        assertEquals(CapabilityRiskPolicy.ALLOW, skill.riskPolicy());
        assertTrue(skill.readOnly());
        assertEquals("skill:20", skill.attributes().get("contentRef"));
        assertEquals("1.2.3", skill.attributes().get("version"));
        assertEquals("Use for code review", skill.attributes().get("trigger"));
        assertEquals(List.of("read_file", "search"), skill.attributes().get("allowedTools"));
        assertEquals("LOW", skill.attributes().get("riskLevel"));
        assertEquals("repo://skills/review", skill.attributes().get("source"));
        assertTrue(String.valueOf(skill.attributes().get("summaryHash")).length() >= 32);

        CapabilityItem mcp = find(plan, CapabilityKind.MCP, "mcp-git");
        assertEquals(CapabilityRiskLevel.MEDIUM, mcp.riskLevel());
        assertEquals(CapabilityRiskPolicy.ASK, mcp.riskPolicy());
        assertEquals("mcp-server:30:protocol-config", mcp.secretRef());
        assertEquals(List.of("mcp-tool:301", "mcp-tool:302"), mcp.includePatterns());
        assertEquals(List.of("mcp-tool:399"), mcp.excludePatterns());
        assertEquals(McpToolExposureMode.SELECTED_ONLY, mcp.attributes().get("exposureMode"));
        assertEquals(List.of("git.status", "git.diff"), mcp.attributes().get("runtimeToolNames"));
        assertEquals(List.of("hash-status", "hash-diff"), mcp.attributes().get("runtimeToolSchemaHashes"));
    }

    @Test
    void capabilityContributorsKeepExpectedRuntimeOrder() {
        RecordingAgentDefinitionService agentDefinitionService = new RecordingAgentDefinitionService();
        List<CapabilityPlanContributor> contributors = new ArrayList<>(List.of(
                new WorkerCapabilityContributor(new EmptyAgentCodeExecutionService(), new EmptyCodeExecutionConfigService()),
                new HookCapabilityContributor(new EmptyAgentHookService(), new EmptyHookConfigService()),
                new ModelPolicyCapabilityContributor(),
                new KnowledgeCapabilityContributor(new EmptyAgentKnowledgeBaseService(), new EmptyKnowledgeBaseConfigService()),
                new SkillCapabilityContributor(agentDefinitionService),
                new SubAgentCapabilityContributor(new EmptyAgentSubAgentService(), agentDefinitionService),
                new McpCapabilityContributor(new EmptyAgentMcpServerService(), new RecordingMcpServerService(), new RecordingMcpToolService()),
                new ToolCapabilityContributor(agentDefinitionService)));

        AnnotationAwareOrderComparator.sort(contributors);

        assertEquals(List.of(
                        ModelPolicyCapabilityContributor.class,
                        ToolCapabilityContributor.class,
                        SkillCapabilityContributor.class,
                        McpCapabilityContributor.class,
                        KnowledgeCapabilityContributor.class,
                        HookCapabilityContributor.class,
                        SubAgentCapabilityContributor.class,
                        WorkerCapabilityContributor.class),
                contributors.stream().map(Object::getClass).toList());
    }

    @Test
    void attributesRejectMalformedEntries() {
        assertThrows(IllegalArgumentException.class, () -> CapabilityPlanItemSupport.attributes("name"));
        assertThrows(IllegalArgumentException.class, () -> CapabilityPlanItemSupport.attributes(100L, "value"));
    }

    private static CapabilityItem find(CapabilityPlan plan, CapabilityKind kind, String name) {
        return plan.items().stream()
                .filter(item -> item.kind() == kind)
                .filter(item -> name.equals(item.name()))
                .findFirst()
                .orElseThrow();
    }

    private static final class RecordingAgentDefinitionService implements AgentDefinitionService {
        int toolsCalled;
        int skillsCalled;

        @Override
        public AgentDefinition getById(Long id) {
            AgentDefinition agent = new AgentDefinition();
            agent.setId(id);
            agent.setName("agent-" + id);
            agent.setEnabled(true);
            agent.setModelConfigId(900L);
            return agent;
        }

        @Override
        public List<ToolConfig> getEnabledToolsOfAgent(Long agentId) {
            toolsCalled++;
            ToolConfig tool = new ToolConfig();
            tool.setId(10L);
            tool.setEnabled(true);
            tool.setName("Search");
            tool.setToolId("tool-search");
            tool.setCategory("builtin");
            tool.setToolType(ToolType.BUILTIN);
            tool.setNeedConfirm(false);
            tool.setVersion("1.0.0");
            return List.of(tool);
        }

        @Override
        public List<SkillPackage> getEnabledSkillsOfAgent(Long agentId) {
            skillsCalled++;
            SkillPackage skill = new SkillPackage();
            skill.setId(20L);
            skill.setEnabled(true);
            skill.setName("skill-review");
            skill.setCategory("coding");
            skill.setDescription("review code changes");
            skill.setSkillContent("""
                    ---
                    name: skill-review
                    description: review code changes
                    version: 1.2.3
                    trigger: Use for code review
                    allowed_tools: read_file, search
                    risk_level: low
                    source: repo://skills/review
                    ---

                    Review the current code changes.
                    """);
            return List.of(skill);
        }

        @Override public IPage<AgentDefinitionVO> pageAgentDefinitions(PageParams p, AgentDefinitionDTO q) { throw unsupported(); }
        @Override public List<AgentDefinition> list() { throw unsupported(); }
        @Override public AgentDefinition getByAgentCode(String agentCode) { throw unsupported(); }
        @Override public AgentDefinitionVO agentDefinitionDetail(Long id) { throw unsupported(); }
        @Override public Boolean saveAgentDefinition(AgentDefinitionVO agentDefinition) { throw unsupported(); }
        @Override public Boolean updateAgentDefinition(AgentDefinitionVO agentDefinition) { throw unsupported(); }
        @Override public Boolean deleteAgentDefinition(List<Long> ids) { throw unsupported(); }
        @Override public List<Object> usedWithAgent(List<Long> ids) { throw unsupported(); }
        @Override public List<String> listTags() { throw unsupported(); }
        @Override public List<String> allowFileType(Long id) { throw unsupported(); }
    }

    private static final class EmptyAgentMcpServerService implements AgentMcpServerService {
        int bindingsCalled;

        @Override
        public List<AgentMcpBindingVO> getBindings(Long agentDefinitionId) {
            bindingsCalled++;
            AgentMcpBindingVO binding = new AgentMcpBindingVO();
            binding.setMcpServerId(30L);
            binding.setExposureMode(McpToolExposureMode.SELECTED_ONLY);
            binding.setMcpToolIds(List.of(301L, 302L));
            return List.of(binding);
        }

        @Override public List<Long> getAgentIds(List<Long> mcpIds) { throw unsupported(); }
        @Override public List<Long> getMcpIds(Long agentDefinitionId) { throw unsupported(); }
        @Override public List<AgentMcpServer> listByAgentDefinitionId(Long agentDefinitionId) { throw unsupported(); }
        @Override public Boolean insertAgentMcpServer(Long agentDefinitionId, List<Long> mcpIds) { throw unsupported(); }
        @Override public Boolean deleteAgentMcpServer(List<Long> agentIds) { throw unsupported(); }
        @Override public Boolean deleteByMcpServerIds(List<Long> mcpServerIds) { throw unsupported(); }
        @Override public Boolean saveAgentMcpServer(Long agentDefinitionId, List<Long> mcpIds, List<AgentMcpBindingVO> bindings) { throw unsupported(); }
    }

    private static final class RecordingMcpServerService implements McpServerService {
        int getByIdCalled;

        @Override
        public McpServer getById(Long id) {
            getByIdCalled++;
            McpServer server = new McpServer();
            server.setId(id);
            server.setEnabled(true);
            server.setName("mcp-git");
            server.setProtocol(McpProtocol.HTTP);
            server.setMode(McpMode.SYNC);
            server.setActivationStatus(McpActivationStatus.ACTIVE);
            server.setToolCount(2);
            server.setNeedsSync(false);
            server.setConfigHash("hash-1");
            return server;
        }

        @Override public IPage<McpServer> page(PageParams pageParams, McpServerDTO query) { throw unsupported(); }
        @Override public boolean save(McpServer entity) { throw unsupported(); }
        @Override public List<Object> usedWithAgent(List<Long> ids) { throw unsupported(); }
        @Override public boolean deleteByIds(List<Long> ids) { throw unsupported(); }
        @Override public McpServer doUpdate(McpServer entity) { throw unsupported(); }
        @Override public McpServer activate(Long id) { throw unsupported(); }
        @Override public McpServer syncTools(Long id) { throw unsupported(); }
        @Override public List<McpToolVO> listTools(Long id) { throw unsupported(); }
        @Override public McpServer updateToolGlobalEnabled(Long id, McpToolEnabledDTO dto) { throw unsupported(); }
    }

    private static final class RecordingMcpToolService implements McpToolService {
        @Override
        public List<McpTool> listRuntimeTools(Long mcpServerId) {
            return List.of(
                    tool(301L, "git.status", true, false, "hash-status"),
                    tool(302L, "git.diff", true, false, "hash-diff"));
        }

        @Override
        public List<McpTool> listByServerIds(List<Long> mcpServerIds) {
            return List.of(
                    tool(301L, "git.status", true, false, "hash-status"),
                    tool(302L, "git.diff", true, false, "hash-diff"),
                    tool(399L, "git.push", false, false, "hash-push"));
        }

        private static McpTool tool(Long id, String name, boolean enabled, boolean missing, String schemaHash) {
            McpTool tool = new McpTool();
            tool.setId(id);
            tool.setMcpServerId(30L);
            tool.setToolName(name);
            tool.setEnabled(enabled);
            tool.setMissing(missing);
            tool.setSchemaHash(schemaHash);
            return tool;
        }

        @Override public List<McpToolVO> listToolVos(Long mcpServerId) { throw unsupported(); }
        @Override public void ensureBackfilledFromCache(McpServer mcpServer) { throw unsupported(); }
        @Override public void syncServerTools(McpServer mcpServer, List<io.modelcontextprotocol.spec.McpSchema.Tool> tools) { throw unsupported(); }
        @Override public void updateGlobalEnabled(Long mcpServerId, List<Long> toolIds, Boolean enabled) { throw unsupported(); }
        @Override public List<McpTool> listByIdsPreserveOrder(List<Long> ids) { throw unsupported(); }
        @Override public Map<Long, Integer> countAvailableTools(List<Long> mcpServerIds) { throw unsupported(); }
        @Override public void deleteByMcpServerIds(List<Long> mcpServerIds) { throw unsupported(); }
    }

    private static final class EmptyAgentSubAgentService implements AgentSubAgentService {
        @Override public List<Long> getSubAgentIds(Long agentDefinitionId) { return List.of(); }
        @Override public Boolean insertSubAgent(Long agentDefinitionId, List<Long> subAgentIds) { throw unsupported(); }
        @Override public Boolean deleteSubAgent(List<Long> agentIds) { throw unsupported(); }
        @Override public Boolean saveSubAgent(Long agentDefinitionId, List<Long> subAgentIds) { throw unsupported(); }
    }

    private static final class EmptyAgentCodeExecutionService implements AgentCodeExecutionService {
        @Override public Long getCodeExecutionIdByAgentId(Long agentId) { return null; }
        @Override public List<Long> getAgentIds(List<Long> codeExecutionIds) { throw unsupported(); }
        @Override public Boolean insertAgentCodeExecution(Long agentDefinitionId, List<Long> codeExecutionIds) { throw unsupported(); }
        @Override public Boolean deleteAgentCodeExecution(List<Long> agentIds) { throw unsupported(); }
        @Override public Boolean saveAgentCodeExecution(Long agentDefinitionId, List<Long> codeExecutionIds) { throw unsupported(); }
    }

    private static final class EmptyCodeExecutionConfigService implements CodeExecutionConfigService {
        @Override public List<com.htam.agent.common.entity.CodeExecutionConfig> list() { throw unsupported(); }
        @Override public com.htam.agent.common.entity.CodeExecutionConfig getById(Long id) { throw unsupported(); }
        @Override public boolean save(com.htam.agent.common.entity.CodeExecutionConfig entity) { throw unsupported(); }
        @Override public boolean updateById(com.htam.agent.common.entity.CodeExecutionConfig entity) { throw unsupported(); }
        @Override public boolean removeByIds(List<Long> ids) { throw unsupported(); }
        @Override public List<Object> usedWithAgent(List<Long> ids) { throw unsupported(); }
    }

    private static final class EmptyAgentHookService implements AgentHookService {
        @Override public List<Long> getHookIds(Long agentDefinitionId) { return List.of(); }
        @Override public List<Long> getAgentIds(List<Long> hookIds) { throw unsupported(); }
        @Override public Boolean insertAgentHook(Long agentDefinitionId, List<Long> hookIds) { throw unsupported(); }
        @Override public Boolean deleteAgentHook(List<Long> agentIds) { throw unsupported(); }
        @Override public Boolean deleteByHookConfigIds(List<Long> hookIds) { throw unsupported(); }
        @Override public Boolean saveAgentHook(Long agentDefinitionId, List<Long> hookIds) { throw unsupported(); }
    }

    private static final class EmptyHookConfigService implements HookConfigService {
        @Override public HookConfig getById(Long id) { throw unsupported(); }
        @Override public IPage<HookConfig> page(PageParams pageParams, HookConfigDTO query) { throw unsupported(); }
        @Override public List<HookConfig> listByIds(List<Long> ids) { throw unsupported(); }
        @Override public boolean save(HookConfig entity) { throw unsupported(); }
        @Override public void SyncConfigToDatabase(List<HookConfigWrapper> configWrappers) { throw unsupported(); }
        @Override public List<Object> usedWithAgent(List<Long> ids) { throw unsupported(); }
        @Override public boolean deleteByIds(List<Long> ids) { throw unsupported(); }
        @Override public boolean doUpdate(HookConfig entity) { throw unsupported(); }
    }

    private static final class EmptyAgentKnowledgeBaseService implements AgentKnowledgeBaseService {
        @Override public List<Long> getKnowledgeIds(Long agentDefinitionId) { return List.of(); }
        @Override public List<Long> getAgentIds(List<Long> knowledgeIds) { throw unsupported(); }
        @Override public Boolean insertAgentKnowledge(Long agentDefinitionId, List<Long> knowledgeIds) { throw unsupported(); }
        @Override public Boolean deleteAgentKnowledge(List<Long> agentIds) { throw unsupported(); }
        @Override public Boolean deleteByKnowledgeIds(List<Long> knowledgeIds) { throw unsupported(); }
        @Override public Boolean saveAgentKnowledge(Long agentDefinitionId, List<Long> knowledgeIds) { throw unsupported(); }
    }

    private static final class EmptyKnowledgeBaseConfigService implements KnowledgeBaseConfigService {
        @Override public KnowledgeBaseConfig getById(Long id) { throw unsupported(); }
        @Override public IPage<KnowledgeBaseConfig> page(PageParams pageParams, KnowledgeBaseConfigDTO query) { throw unsupported(); }
        @Override public boolean save(KnowledgeBaseConfig entity) { throw unsupported(); }
        @Override public List<Object> usedWithAgent(List<Long> ids) { throw unsupported(); }
        @Override public KnowledgeBaseConfig getByAgentId(Long agentId) { throw unsupported(); }
        @Override public boolean deleteByIds(List<Long> ids) { throw unsupported(); }
        @Override public boolean doUpdate(KnowledgeBaseConfig entity) { throw unsupported(); }
    }

    private static UnsupportedOperationException unsupported() {
        return new UnsupportedOperationException("not needed by this test");
    }
}
