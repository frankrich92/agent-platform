package com.htam.agent.capability.skill.imports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SkillPackageReaderTest {

    @TempDir
    Path tempDir;

    @Test
    void readSkipsBinaryResourcesButKeepsTextResources() throws IOException {
        Path skillDir = tempDir.resolve("demo-skill");
        Path scriptsDir = skillDir.resolve("scripts");
        Files.createDirectories(scriptsDir);
        Files.writeString(skillDir.resolve("SKILL.md"), """
                ---
                name: demo-skill
                description: Demo skill
                ---

                Use this skill for tests.
                """, StandardCharsets.UTF_8);
        Files.writeString(scriptsDir.resolve("run.sh"), "echo ok\n", StandardCharsets.UTF_8);
        Files.write(scriptsDir.resolve("archive.tar.gz"), new byte[] {(byte) 0x8B, 0x00, 0x01});

        ImportedSkill importedSkill = SkillPackageReader.read(tempDir, "demo-skill");

        assertEquals("demo-skill", importedSkill.name());
        assertEquals("Demo skill", importedSkill.description());
        assertEquals("echo ok\n", importedSkill.resources().get("scripts/run.sh"));
        assertFalse(importedSkill.resources().containsKey("scripts/archive.tar.gz"));
        assertTrue(SkillPackageReader.skillNames(tempDir).contains("demo-skill"));
    }
}
