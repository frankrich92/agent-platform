package com.htam.agent.skill.imports.source;

import com.htam.agent.skill.imports.config.LocalImportConfig;
import io.agentscope.core.skill.repository.AgentSkillRepository;
import io.agentscope.core.skill.repository.FileSystemSkillRepository;

import java.nio.file.Path;

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
    public AgentSkillRepository repository() {
        return repository;
    }

    @Override
    public void close() {
        repository.close();
    }
}
