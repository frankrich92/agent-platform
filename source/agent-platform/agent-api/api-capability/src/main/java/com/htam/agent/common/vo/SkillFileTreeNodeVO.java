package com.htam.agent.common.vo;

import com.htam.agent.common.config.SerializableEnable;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 技能包文件树节点。
 */
@Data
public class SkillFileTreeNodeVO implements SerializableEnable {
    private String name;
    private String path;
    private boolean directory;
    private Long fileId;
    private String fileType;
    private String extension;
    private long fileSize;
    private List<SkillFileTreeNodeVO> children = new ArrayList<>();
}
