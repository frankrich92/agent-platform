package com.htam.agent.adapter.rest.profile.agent.controller;

import com.htam.agent.profile.agent.service.AgentChatKeyService;
import com.htam.agent.common.config.auth.ChatKeyAccess;
import com.htam.agent.common.config.auth.SkAccess;
import com.htam.agent.common.r.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 描述：智能体对话Key Controller
 *
 * @author huxuehao
 **/
@RestController
@RequestMapping("/agent/chat-key")
@RequiredArgsConstructor
public class AgentChatKeyController {
    private final AgentChatKeyService agentChatKeyService;

    /**
     * 获取chat key
     * @param agentId agent Id
     * @param refresh 是否刷新key
     */
    @SkAccess
    @ChatKeyAccess
    @GetMapping("/{agentId}")
    public R<String> getChatKey(@PathVariable("agentId") Long agentId,
                                @RequestParam("refresh") boolean refresh) {
        return R.data(agentChatKeyService.getChatKey(agentId, refresh));
    }

    @SkAccess
    @ChatKeyAccess
    @GetMapping("/{chatKey}/get-agent-id")
    public R<Long> getAgentIdByChatKey(@PathVariable("chatKey") String chatKey) {
        return R.data(agentChatKeyService.getAgentIdByChatKey(chatKey));
    }
}
