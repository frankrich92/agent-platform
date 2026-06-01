package com.htam.agent.common.vo;

import com.htam.agent.common.config.SerializableEnable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模型连接性检测结果。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckModelResult implements SerializableEnable {
    private Boolean success;
    private String message;
}
