package com.htam.agent.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 技能包入库文件类型。
 */
@Getter
@AllArgsConstructor
public enum SkillFileType {
    SKILL_MD("SKILL.md"),
    REFERENCES("参考资源"),
    EXAMPLES("示例"),
    SCRIPTS("脚本");

    private final String description;
}
