package com.htam.agent.job.core.enable;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 描述：是否启用标记
 *
 * @author huxuehao
 **/
@Component
@ConditionalOnProperty(name="agent.quartz.enabled", havingValue = "true", matchIfMissing = true)
public class QuartzEnabled {
}
