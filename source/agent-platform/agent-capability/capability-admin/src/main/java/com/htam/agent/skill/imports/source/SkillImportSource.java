package com.htam.agent.skill.imports.source;

import io.agentscope.core.skill.repository.AgentSkillRepository;

import java.nio.file.Path;

/**
 * Prepared source for one skill import run.
 */
public interface SkillImportSource extends AutoCloseable {
    Path skillsDir();

    AgentSkillRepository repository();

    @Override
    void close();
}
