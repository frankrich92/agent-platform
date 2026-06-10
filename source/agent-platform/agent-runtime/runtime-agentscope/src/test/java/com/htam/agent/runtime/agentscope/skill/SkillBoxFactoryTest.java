package com.htam.agent.runtime.agentscope.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import com.htam.agent.common.consts.SysConst;
import com.htam.agent.common.entity.AgentCodeExecution;
import com.htam.agent.common.entity.AgentDefinition;
import com.htam.agent.common.entity.AgentSkillPackage;
import com.htam.agent.common.entity.CodeExecutionConfig;
import com.htam.agent.common.entity.SkillFile;
import com.htam.agent.common.entity.SkillPackage;
import com.htam.agent.common.entity.SkillTool;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.repo.agent.AgentCodeExecutionRepository;
import com.htam.agent.repo.agent.CodeExecutionConfigRepository;
import com.htam.agent.repo.capability.AgentSkillPackageRepository;
import com.htam.agent.repo.capability.SkillFileRepository;
import com.htam.agent.repo.capability.SkillPackageRepository;
import com.htam.agent.repo.capability.SkillToolRepository;
import com.htam.agent.repo.support.RepoPage;
import com.htam.agent.runtime.agentscope.agui.AgentContext;
import io.agentscope.core.skill.SkillBox;
import io.agentscope.core.tool.Toolkit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class SkillBoxFactoryTest {

    @AfterEach
    void cleanAgentContext() {
        AgentContext.clean();
    }

    @Test
    void registersLoadSkillToolOnProvidedToolkit() {
        Toolkit toolkit = new Toolkit();
        SkillBoxFactory factory = factory(null);

        factory.getSkillBox(agentDefinition(), toolkit);

        assertTrue(toolkit.getToolNames().contains("load_skill_content"));
    }

    @Test
    void configuresCodeExecutionBeforeSkillBoxIsReturned() {
        AgentContext.get().setThreadId("thread-1");
        CodeExecutionConfig config = new CodeExecutionConfig();
        config.setEnableRead(true);
        SkillBoxFactory factory = factory(config);

        SkillBox skillBox = factory.getSkillBox(agentDefinition(), new Toolkit());

        assertTrue(skillBox.getCodeExecutionWorkDir().toString().endsWith(SysConst.WORKSPACE_PATH + "/thread-1"));
        assertTrue(skillBox.getAllSkillIds().contains("workspace_path_and_execution_rules_custom"));
    }

    private static AgentDefinition agentDefinition() {
        AgentDefinition definition = new AgentDefinition();
        definition.setId(10L);
        return definition;
    }

    private static SkillBoxFactory factory(CodeExecutionConfig codeExecutionConfig) {
        return new SkillBoxFactory(
                null,
                new EmptySkillToolRepository(),
                new EmptySkillPackageRepository(),
                new EmptySkillFileRepository(),
                new EmptyAgentSkillPackageRepository(),
                new StaticAgentCodeExecutionRepository(codeExecutionConfig == null ? null : 20L),
                new StaticCodeExecutionConfigRepository(codeExecutionConfig));
    }

    private static final class EmptyAgentSkillPackageRepository implements AgentSkillPackageRepository {
        @Override
        public List<AgentSkillPackage> listBySkillPackageIds(List<Long> skillPackageIds) {
            return List.of();
        }

        @Override
        public List<AgentSkillPackage> listByAgentDefinitionId(Long agentDefinitionId) {
            return List.of();
        }

        @Override
        public boolean save(AgentSkillPackage agentSkillPackage) {
            return true;
        }

        @Override
        public boolean deleteByAgentDefinitionIds(List<Long> agentIds) {
            return true;
        }

        @Override
        public boolean deleteBySkillPackageIds(List<Long> skillPackageIds) {
            return true;
        }
    }

    private static final class StaticAgentCodeExecutionRepository implements AgentCodeExecutionRepository {
        private final Long codeExecutionId;

        private StaticAgentCodeExecutionRepository(Long codeExecutionId) {
            this.codeExecutionId = codeExecutionId;
        }

        @Override
        public List<AgentCodeExecution> listByCodeExecutionIds(List<Long> codeExecutionIds) {
            return List.of();
        }

        @Override
        public AgentCodeExecution getByAgentId(Long agentId) {
            return codeExecutionId == null ? null : new AgentCodeExecution(1L, agentId, codeExecutionId);
        }

        @Override
        public boolean save(AgentCodeExecution entity) {
            return true;
        }

        @Override
        public boolean deleteByAgentIds(List<Long> agentIds) {
            return true;
        }
    }

    private static final class StaticCodeExecutionConfigRepository implements CodeExecutionConfigRepository {
        private final CodeExecutionConfig config;

        private StaticCodeExecutionConfigRepository(CodeExecutionConfig config) {
            this.config = config;
        }

        @Override
        public List<CodeExecutionConfig> list() {
            return List.of();
        }

        @Override
        public CodeExecutionConfig getById(Long id) {
            return config;
        }

        @Override
        public boolean save(CodeExecutionConfig entity) {
            return true;
        }

        @Override
        public boolean updateById(CodeExecutionConfig entity) {
            return true;
        }

        @Override
        public boolean deleteByIds(List<Long> ids) {
            return true;
        }
    }

    private static final class EmptySkillPackageRepository implements SkillPackageRepository {
        @Override
        public RepoPage<SkillPackage> page(PageParams pageParams, String name, String category, Boolean enabled) {
            return new RepoPage<>(List.of(), 0, 0, 1);
        }

        @Override
        public SkillPackage getById(Long id) {
            return null;
        }

        @Override
        public SkillPackage getByName(String name) {
            return null;
        }

        @Override
        public List<SkillPackage> listByIds(List<Long> ids) {
            return List.of();
        }

        @Override
        public List<SkillPackage> listAll() {
            return List.of();
        }

        @Override
        public List<SkillPackage> listEnabledBriefByIds(List<Long> ids) {
            return List.of();
        }

        @Override
        public List<SkillPackage> listWithScripts() {
            return List.of();
        }

        @Override
        public boolean save(SkillPackage entity) {
            return true;
        }

        @Override
        public boolean updateById(SkillPackage entity) {
            return true;
        }

        @Override
        public boolean deleteByIds(List<Long> ids) {
            return true;
        }

        @Override
        public List<String> listCategories() {
            return List.of();
        }
    }

    private static final class EmptySkillFileRepository implements SkillFileRepository {
        @Override
        public List<SkillFile> listBySkillId(Long skillId) {
            return List.of();
        }

        @Override
        public SkillFile getById(Long id) {
            return null;
        }

        @Override
        public SkillFile getBySkillIdAndPath(Long skillId, String filePath) {
            return null;
        }

        @Override
        public boolean save(SkillFile entity) {
            return true;
        }

        @Override
        public boolean updateById(SkillFile entity) {
            return true;
        }

        @Override
        public boolean deleteById(Long id) {
            return true;
        }

        @Override
        public boolean deleteBySkillId(Long skillId) {
            return true;
        }

        @Override
        public boolean deleteBySkillIds(List<Long> skillIds) {
            return true;
        }

        @Override
        public boolean removeBySkillIdAndPath(Long skillId, String filePath) {
            return true;
        }

        @Override
        public boolean removeBySkillIdAndPathPrefix(Long skillId, String pathPrefix) {
            return true;
        }
    }

    private static final class EmptySkillToolRepository implements SkillToolRepository {
        @Override
        public List<SkillTool> listBySkillId(Long skillId) {
            return List.of();
        }

        @Override
        public List<SkillTool> listByToolIds(List<Long> toolIds) {
            return List.of();
        }

        @Override
        public boolean save(SkillTool skillTool) {
            return true;
        }

        @Override
        public boolean deleteBySkillIds(List<Long> skillIds) {
            return true;
        }

        @Override
        public boolean deleteByToolIds(List<Long> toolIds) {
            return true;
        }
    }
}
