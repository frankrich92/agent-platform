package com.htam.agent.run.session.service.impl;

import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.htam.agent.repo.agent.AgentDefinitionRepository;
import com.htam.agent.run.message.service.ChatMessageService;
import com.htam.agent.run.session.service.ChatSessionService;
import com.htam.agent.common.consts.SysConst;
import com.htam.agent.common.dto.ChatMessageAppendDTO;
import com.htam.agent.common.dto.ChatSessionCreateDTO;
import com.htam.agent.common.dto.ChatSessionQueryDTO;
import com.htam.agent.common.entity.AgentDefinition;
import com.htam.agent.common.entity.ChatMessage;
import com.htam.agent.common.entity.ChatSession;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.common.util.BeanUtils;
import com.htam.agent.common.util.FolderUtils;
import com.htam.agent.common.util.UserUtils;
import com.htam.agent.common.vo.ChatMessageVO;
import com.htam.agent.common.vo.ChatMessagePageVO;
import com.htam.agent.common.vo.ChatSessionVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htam.agent.repo.agent.ChatSessionRepository;
import com.htam.agent.repo.support.RepoPage;
import com.htam.agent.runtime.AgentRuntimeSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 聊天会话 Service 实现：新会话插根消息并设 current_message_id；追加/重新生成插新消息并更新 current_message_id；切换分支仅更新 current_message_id；回显按 path 查消息链。
 *
 * @author huxuehao
 */
@Service
@RequiredArgsConstructor
public class ChatSessionServiceImpl implements ChatSessionService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageService chatMessageService;
    private final AgentRuntimeSessionService runtimeSessionService;
    private final AgentDefinitionRepository agentDefinitionRepository;

    @Override
    @DSTransactional(rollbackFor = Exception.class)
    public ChatSessionVO createSession(ChatSessionCreateDTO dto) {
        Long userId = UserUtils.getId();
        if (userId == null || userId == 0L) {
            throw new RuntimeException("用户未登录");
        }
        if (dto.getAgentId() == null ) {
            throw new RuntimeException("agentId 不能为空");
        }

        // 校验智能体的合法性
        AgentDefinition agentDefinition = agentDefinitionRepository.getById(dto.getAgentId());

        if (agentDefinition == null || !Boolean.TRUE.equals(agentDefinition.getEnabled())) {
            throw new RuntimeException("智能体不存在或已禁用");
        }

        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setAgentId(dto.getAgentId());
        session.setTitle(dto.getTitle() != null ? dto.getTitle() : "新对话");

        chatSessionRepository.save(session);

        ChatMessage root = new ChatMessage();
        root.setSessionId(session.getId());
        root.setRole("system");
        root.setContent("");
        root.setParentId(null);
        chatMessageService.save(root);
        root.setPath(String.valueOf(root.getId()));
        root.setDepth(0);
        chatMessageService.updateById(root);

        session.setCurrentMessageId(root.getId());
        chatSessionRepository.updateById(session);

        if (dto.getInitWorkspace() != null && dto.getInitWorkspace()) {
            FolderUtils.mkdirsByRelativePath(SysConst.WORKSPACE_PATH + "/" + session.getId());
        }

        return toSessionVO(session);
    }

    @Override
    @DSTransactional(rollbackFor = Exception.class)
    public ChatMessageVO appendMessage(Long sessionId, ChatMessageAppendDTO dto) {
        ChatSession session = getAndCheckSession(sessionId);
        ChatMessage parent = getMessageBy(session.getCurrentMessageId(), sessionId);
        return saveNewMessageAndMoveCursor(session, parent, dto.getRole(), dto.getContent());
    }

    @Override
    @DSTransactional(rollbackFor = Exception.class)
    public ChatMessageVO regenerateMessage(Long sessionId, ChatMessageAppendDTO dto) {
        ChatSession session = getAndCheckSession(sessionId);
        ChatMessage parent = getMessageBy(session.getCurrentMessageId(), sessionId);
        return saveNewMessageAndMoveCursor(session, parent, dto.getRole(), dto.getContent());
    }

    @Override
    @DSTransactional(rollbackFor = Exception.class)
    public void switchCurrentMessage(Long sessionId, Integer messageId) {
        ChatSession session = getAndCheckSession(sessionId);
        ChatMessage message = chatMessageService.getById(messageId);
        if (message == null || !message.getSessionId().equals(sessionId)) {
            throw new RuntimeException("消息不存在或不属于该会话");
        }
        session.setCurrentMessageId(messageId);
        chatSessionRepository.updateById(session);
    }

    @Override
    public List<ChatMessageVO> getCurrentMessages(Long sessionId) {
        ChatSession session = chatSessionRepository.getById(sessionId);
        if (session == null) {
            return new ArrayList<>();
        }
        Integer curId = session.getCurrentMessageId();
        if (curId == null) {
            return new ArrayList<>();
        }
        ChatMessage cur = chatMessageService.getById(curId);
        if (cur == null) {
            return new ArrayList<>();
        }
        String path = cur.getPath();
        if (path == null || path.isEmpty()) {
            return new ArrayList<>();
        }
        List<Integer> ids = Arrays.stream(path.split("/"))
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toList());
        if (ids.isEmpty()) {
            ids.add(curId);
        }
        List<ChatMessage> list = chatMessageService.listByIdsOrderByDepth(ids);
        return BeanUtils.copyList(list, ChatMessageVO.class);
    }
    @Override
    public ChatMessagePageVO getCurrentMessagesPaged(Long sessionId, Integer beforeDepth, int size) {
        // 1. 解析当前路径上的所有消息 ID
        ChatSession session = chatSessionRepository.getById(sessionId);
        ChatMessagePageVO result = new ChatMessagePageVO();
        if (session == null || session.getCurrentMessageId() == null) {
            result.setMessages(new ArrayList<>());
            result.setHasMore(false);
            return result;
        }
        ChatMessage cur = chatMessageService.getById(session.getCurrentMessageId());
        if (cur == null || cur.getPath() == null || cur.getPath().isEmpty()) {
            result.setMessages(new ArrayList<>());
            result.setHasMore(false);
            return result;
        }
        List<Integer> ids = Arrays.stream(cur.getPath().split("/"))
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toList());
        if (ids.isEmpty()) {
            ids.add(session.getCurrentMessageId());
        }

        // 2. 查询该路径上全部消息并按 depth 升序
        List<ChatMessage> allMessages = chatMessageService.listByIdsOrderByDepth(ids);

        // 3. 根据游标切片
        List<ChatMessage> page;
        if (beforeDepth == null) {
            // 首次加载：取末尾 size 条
            int from = Math.max(0, allMessages.size() - size);
            page = allMessages.subList(from, allMessages.size());
        } else {
            // 加载 beforeDepth 之前的消息：取 depth < beforeDepth 的末尾 size 条
            List<ChatMessage> candidates = allMessages.stream()
                    .filter(m -> m.getDepth() < beforeDepth)
                    .collect(Collectors.toList());
            int from = Math.max(0, candidates.size() - size);
            page = candidates.subList(from, candidates.size());
        }

        // 4. 判断是否还有更早的消息
        boolean hasMore;
        if (page.isEmpty()) {
            hasMore = false;
        } else {
            int earliestDepth = page.get(0).getDepth();
            hasMore = allMessages.stream().anyMatch(m -> m.getDepth() < earliestDepth);
        }

        result.setMessages(BeanUtils.copyList(page, ChatMessageVO.class));
        result.setHasMore(hasMore);
        result.setNextBeforeDepth(page.isEmpty() ? null : page.get(0).getDepth());
        return result;
    }



    @Override
    public List<ChatSessionVO> listSessions(ChatSessionQueryDTO query) {
        Long userId = query.getUserId() != null ? query.getUserId() : UserUtils.getId();
        return chatSessionRepository.listSessions(userId, query.getAgentId())
                .stream()
                .map(this::toSessionVO)
                .collect(Collectors.toList());
    }

    @Override
    public IPage<ChatSessionVO> pageSessions(PageParams pageParams, ChatSessionQueryDTO query) {
        Long userId = query.getUserId() != null ? query.getUserId() : UserUtils.getId();
        RepoPage<ChatSession> repoPage = chatSessionRepository.pageSessions(
                pageParams,
                userId,
                query.getAgentId(),
                query.getIsPinned());
        IPage<ChatSession> page = new Page<>(repoPage.current(), repoPage.size(), repoPage.total());
        page.setRecords(repoPage.records());
        return BeanUtils.copyPage(page, ChatSessionVO.class);
    }

    @Override
    public ChatSessionVO getSessionDetail(Long id) {
        ChatSession session = chatSessionRepository.getById(id);
        if (session == null) {
            return null;
        }
        return toSessionVO(session);
    }

    private ChatSession getAndCheckSession(Long sessionId) {
        ChatSession session = chatSessionRepository.getById(sessionId);
        if (session == null) {
            throw new RuntimeException("会话不存在或已删除");
        }
        if (!session.getUserId().equals(UserUtils.getId())) {
            throw new RuntimeException("无权限操作该会话");
        }
        return session;
    }

    private ChatMessage getMessageBy(Integer messageId, Long sessionId) {
        ChatMessage msg = chatMessageService.getById(messageId);
        if (msg == null || !msg.getSessionId().equals(sessionId)) {
            throw new RuntimeException("当前消息不存在或不属于该会话");
        }
        return msg;
    }

    /**
     * 在父消息后插入新消息并更新会话的 current_message_id（用于正常追加与重新生成）
     */
    private ChatMessageVO saveNewMessageAndMoveCursor(ChatSession session, ChatMessage parent, String role, String content) {
        if (role == null || content == null) {
            throw new RuntimeException("role 与 content 不能为空");
        }

        ChatMessage msg = new ChatMessage();
        msg.setSessionId(session.getId());
        msg.setRole(role);
        msg.setContent(content);
        msg.setParentId(parent.getId());
        chatMessageService.save(msg);

        String parentPath = parent.getPath();
        String newPath = (parentPath == null || parentPath.isEmpty())
                ? String.valueOf(msg.getId())
                : parentPath + "/" + msg.getId();
        msg.setPath(newPath);
        msg.setDepth((parent.getDepth() == null ? 0 : parent.getDepth()) + 1);
        chatMessageService.updateById(msg);

        session.setCurrentMessageId(msg.getId());
        chatSessionRepository.updateById(session);

        return BeanUtils.copy(msg, ChatMessageVO.class);
    }

    @Override
    @DSTransactional(rollbackFor = Exception.class)
    public void pinSession(Long id) {
        ChatSession session = getAndCheckSession(id);
        session.setIsPinned(true);
        session.setPinTime(java.time.LocalDateTime.now());
        chatSessionRepository.updateById(session);
    }

    @Override
    @DSTransactional(rollbackFor = Exception.class)
    public void unpinSession(Long id) {
        ChatSession session = getAndCheckSession(id);
        session.setIsPinned(false);
        session.setPinTime(null);
        chatSessionRepository.updateById(session);
    }

    @Override
    @DSTransactional(rollbackFor = Exception.class)
    public void updateTitle(Long id, String title) {
        ChatSession session = getAndCheckSession(id);
        session.setTitle(title != null ? title : "新对话");
        chatSessionRepository.updateById(session);
    }

    @Override
    @DSTransactional(rollbackFor = Exception.class)
    public void deleteSession(Long id) {
        ChatSession session = getAndCheckSession(id);
        chatMessageService.deleteBySessionId(session.getId());
        chatSessionRepository.deleteById(id);

        runtimeSessionService.deleteSession(String.valueOf(session.getId()));

        // 删除工作空间目录（文件 + sessionId文件夹本身）
        String workspacePath = SysConst.WORKSPACE_PATH + "/" + session.getId();
        FolderUtils.deleteRecursively(workspacePath);
    }

    private ChatSessionVO toSessionVO(ChatSession session) {
        return BeanUtils.copy(session, ChatSessionVO.class);
    }
}
