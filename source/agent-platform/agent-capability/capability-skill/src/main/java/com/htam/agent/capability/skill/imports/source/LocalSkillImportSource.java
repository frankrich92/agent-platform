package com.htam.agent.capability.skill.imports.source;

import com.htam.agent.capability.skill.imports.config.LocalImportConfig;
import com.htam.agent.capability.skill.imports.ImportedSkill;
import com.htam.agent.capability.skill.imports.SkillPackageReader;

import java.nio.file.Path;
import java.util.List;

public class LocalSkillImportSource implements SkillImportSource {
    private final Path skillsDir;

    public LocalSkillImportSource(LocalImportConfig config) {
        this.skillsDir = Path.of(config.getPath());
    }

    @Override
    public Path skillsDir() {
        return skillsDir;
    }

    @Override
    public List<String> skillNames() {
        return SkillPackageReader.skillNames(skillsDir);
    }

    @Override
    public ImportedSkill skill(String skillName) {
        return SkillPackageReader.read(skillsDir, skillName);
    }

    @Override
    public void close() {
    }
}
