package com.htam.agent.adapter.agui.run;

import com.htam.agent.common.util.UserUtils;
import io.agentscope.core.agui.observer.AguiRequestUserProvider;
import org.springframework.stereotype.Component;

@Component
public class UserUtilsAguiRequestUserProvider implements AguiRequestUserProvider {

    @Override
    public Long currentUserId() {
        Long userId = UserUtils.getId();
        return userId == null || userId <= 0 ? null : userId;
    }
}
