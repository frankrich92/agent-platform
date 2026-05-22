package com.htam.agent.common.mp.support;

import com.htam.agent.common.config.SerializableEnable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 描述：分页参数
 *
 * @author huxuehao
 */
@Setter
@Getter
public class PageParams implements SerializableEnable {
    private Integer page;
    private Integer size;

    private List<String> asc;
    private List<String> desc;
}
