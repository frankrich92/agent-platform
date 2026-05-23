package com.htam.agent.params.core.impl;

import com.htam.agent.common.consts.SysConst;
import com.htam.agent.common.util.FuncUtils;
import com.htam.agent.params.core.ParamsAdapter;
import com.htam.agent.params.core.ParamsCore;
import org.springframework.stereotype.Component;

/**
 * 描述：允许上传的文件类型
 *
 * @author huxuehao
 **/
@Component
public class AllowAudioUploadFile implements ParamsCore {
    @Override
    public String checkAndFormatValue(String value) {
        if(FuncUtils.isEmpty(value)) {
            throw new RuntimeException("value值不可为空");
        }
        String trim = value.trim();
        trim = trim.replace(" ", ",");
        trim = trim.replace("，", ",");
        trim = trim.replace("、", ",");
        trim = trim.replace(";", ",");
        trim = trim.replace("；", ",");
        return trim;
    }

    @Override
    public String getDefaultValue() {
        return SysConst.ALLOW_AUDIO_FILE_TYPE;
    }

    @Override
    public void register(ParamsAdapter adapter) {
        adapter.register("ALLOW_AUDIO_FILE_TYPE", new AllowAudioUploadFile());
    }
}
