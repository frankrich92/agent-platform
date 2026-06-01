package com.htam.agent.governance.system.params.core.impl;

import com.htam.agent.common.util.FuncUtils;
import com.htam.agent.governance.system.params.core.ParamsAdapter;
import com.htam.agent.governance.system.params.core.ParamsCore;
import org.springframework.stereotype.Component;

/**
 * 技能包文件允许入库的扩展名白名单。
 */
@Component
public class SkillFileAllowedExtensionsParams implements ParamsCore {
    @Override
    public String checkAndFormatValue(String value) {
        if (FuncUtils.isEmpty(value)) {
            throw new RuntimeException("value值不可为空");
        }
        return value.trim();
    }

    @Override
    public String getDefaultValue() {
        return "md,py,sh,js,ts,json,yaml,yml,xml,txt,java,cs,go,rs,rb,php,sql,html,css,scss,less,cfg,conf,toml";
    }

    @Override
    public void register(ParamsAdapter adapter) {
        adapter.register("SKILL_FILE_ALLOWED_EXTENSIONS", new SkillFileAllowedExtensionsParams());
    }
}
