package com.htam.agent.capability.skill.imports.source;

import com.htam.agent.capability.skill.imports.config.LocalImportConfig;
import com.htam.agent.capability.skill.imports.ImportedSkill;
import io.agentscope.core.skill.repository.FileSystemSkillRepository;

import java.nio.file.Path;
import java.util.List;

public class LocalSkillImportSource implements SkillImportSource {
    private final Path skillsDir;
    private final FileSystemSkillRepository repository;

    public LocalSkillImportSource(LocalImportConfig config) {
        this.skillsDir = Path.of(config.getPath());
        this.repository = new FileSystemSkillRepository(skillsDir);
    }

    @Override
    public Path skillsDir() {
        return skillsDir;
    }

    @Override
    public List<String> skillNames() {
        return repository.getAllSkillNames();
    }

    @Override
    public ImportedSkill skill(String skillName) {
        return AgentScopeSkillMapper.fromAgentScope(repository.getSkill(skillName));
    }

    @Override
    public void close() {
        repository.close();
    }
}
