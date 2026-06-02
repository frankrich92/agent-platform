package com.htam.agent.capability.skill.cluster;

import com.htam.agent.capability.skill.SkillFileSystemService;
import com.htam.agent.common.consts.RedisChannelTopic;
import com.htam.agent.common.message.ParamChangeMessage;
import com.htam.agent.common.util.JsonUtils;
import com.htam.agent.run.event.cluster.core.ChannelSubscriber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.Topic;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SkillCacheSubscriber implements ChannelSubscriber {
    private static final String SKILL_EXTENSIONS_KEY = "SKILL_FILE_ALLOWED_EXTENSIONS";

    @Override
    public Topic getTopic() {
        return new ChannelTopic(RedisChannelTopic.PARAM_CHANGE_CHANNEL);
    }

    @Override
    public void onMessage(String channel, String message) {
        if (!RedisChannelTopic.PARAM_CHANGE_CHANNEL.equals(channel)) {
            return;
        }
        try {
            ParamChangeMessage msg = JsonUtils.parse(message, ParamChangeMessage.class);
            if (msg != null && SKILL_EXTENSIONS_KEY.equals(msg.getParamKey())) {
                SkillFileSystemService.clearExtensionCache();
                log.info("参数变更触发技能扩展名缓存清除 - paramKey: {}, fromNode: {}",
                        msg.getParamKey(), msg.getSourceNodeId());
            }
        } catch (Exception e) {
            log.error("处理参数变更消息失败", e);
        }
    }
}
