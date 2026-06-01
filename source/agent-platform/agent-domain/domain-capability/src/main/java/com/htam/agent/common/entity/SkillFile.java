package com.htam.agent.common.entity;

import com.htam.agent.common.consts.TableConst;
import com.htam.agent.common.enums.SkillFileType;
import com.htam.agent.common.persistence.DomainTable;
import lombok.Getter;
import lombok.Setter;

/**
 * 技能包入库文件。
 */
@Getter
@Setter
@DomainTable(TableConst.SKILL_FILE)
public class SkillFile extends BaseEntity {
    private Long skillId;
    private SkillFileType fileType;
    private String fileName;
    private String filePath;
    private String content;
    private Integer sort;
}
