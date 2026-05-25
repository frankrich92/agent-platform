package com.htam.agent.capability.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.htam.agent.common.entity.SkillPackage;
import com.htam.agent.common.skill.SkillMetadata;
import java.util.List;
import org.junit.jupiter.api.Test;

class SkillMetadataTest {

    @Test
    void parsesGovernanceFrontmatterAndBuildsHashFallback() {
        SkillPackage skill = new SkillPackage();
        skill.setSkillContent("""
                ---
                name: demo
                version: 1.0.0
                trigger: Use for demos
                allowed_tools: read_file, search, read_file
                risk_level: medium
                source: repo://skills/demo
                ---

                Demo skill content.
                """);

        SkillMetadata metadata = SkillMetadata.from(skill);

        assertEquals("1.0.0", metadata.version());
        assertEquals("Use for demos", metadata.trigger());
        assertEquals(List.of("read_file", "search"), metadata.allowedTools());
        assertEquals("MEDIUM", metadata.riskLevel());
        assertEquals("repo://skills/demo", metadata.source());
        assertFalse(metadata.summaryHash().isBlank());
    }
}
