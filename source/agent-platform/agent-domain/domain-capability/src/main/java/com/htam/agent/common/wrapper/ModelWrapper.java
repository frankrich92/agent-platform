package com.htam.agent.common.wrapper;

import com.htam.agent.common.entity.ModelConfig;
import com.htam.agent.common.entity.ModelProvider;
import lombok.*;

/**
 * 描述：模型包装类
 *
 * @author huxuehao
 **/
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelWrapper {
    private ModelConfig config;
    private ModelProvider provider;
}
