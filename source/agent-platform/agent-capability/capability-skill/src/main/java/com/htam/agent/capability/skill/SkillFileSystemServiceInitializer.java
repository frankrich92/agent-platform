package com.htam.agent.capability.skill;

import com.htam.agent.governance.system.params.core.ParamsAdapter;
import org.springframework.stereotype.Component;

@Component
public class SkillFileSystemServiceInitializer {
    public SkillFileSystemServiceInitializer(ParamsAdapter paramsAdapter) {
        SkillFileSystemService.setParamsAdapter(paramsAdapter);
    }
}
