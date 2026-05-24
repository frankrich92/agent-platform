package com.htam.agent.capability.skill.imports.source;

import com.htam.agent.capability.skill.imports.ImportedSkill;

import java.nio.file.Path;
import java.util.List;

/**
 * Prepared source for one skill import run.
 */
public interface SkillImportSource extends AutoCloseable {
    Path skillsDir();

    List<String> skillNames();

    ImportedSkill skill(String skillName);

    @Override
    void close();
}
