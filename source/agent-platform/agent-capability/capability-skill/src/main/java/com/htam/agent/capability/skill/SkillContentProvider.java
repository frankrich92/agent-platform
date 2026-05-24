package com.htam.agent.capability.skill;

import java.util.Optional;

public interface SkillContentProvider {

    Optional<String> loadContent(String skillId, String contentRef);
}
