/*
 * Copyright 2024-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.agentscope.core.agui.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.htam.agent.runtime.agentscope.attachment.UploadedAttachmentRef;
import com.htam.agent.runtime.agentscope.attachment.UploadedAttachmentReferences;
import com.htam.agent.runtime.agentscope.agui.AgentContext;
import io.agentscope.core.agui.model.AguiFunctionCall;
import io.agentscope.core.agui.model.AguiMessage;
import io.agentscope.core.agui.model.AguiToolCall;
import io.agentscope.core.message.*;
import io.agentscope.core.util.JsonException;
import io.agentscope.core.util.JsonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Converter between AG-UI messages and AgentScope messages.
 *
 * <p>This class handles the bidirectional conversion between the AG-UI protocol's
 * message format and AgentScope's internal message format.
 */
public class AguiMessageConverter {
    /**
     * Creates a new AguiMessageConverter
     */
    public AguiMessageConverter() {
    }

    /**
     * Convert an AG-UI message to an AgentScope message.
     *
     * @param aguiMessage The AG-UI message to convert
     * @return The converted AgentScope message
     */
    public Msg toMsg(AguiMessage aguiMessage) {
        MsgRole role = convertRole(aguiMessage.getRole());
        List<ContentBlock> blocks = new ArrayList<>();

        // Add text content if present
        if (aguiMessage.getContent() != null && !aguiMessage.getContent().isEmpty()) {
            if (aguiMessage.isToolMessage() && aguiMessage.getToolCallId() != null) {
                // For tool messages, wrap content in ToolResultBlock
                blocks.add(
                        ToolResultBlock.of(
                                aguiMessage.getToolCallId(),
                                null,
                                TextBlock.builder().text(aguiMessage.getContent()).build()));
            } else {
                blocks.add(TextBlock.builder().text(aguiMessage.getContent()).build());
            }
        }

        // Add tool calls if present (for assistant messages)
        if (aguiMessage.hasToolCalls()) {
            for (AguiToolCall tc : aguiMessage.getToolCalls()) {
                blocks.add(toToolUseBlock(tc));
            }
        }

        return Msg.builder().id(aguiMessage.getId()).role(role).content(blocks).build();
    }

    /**
     * Convert an AgentScope message to an AG-UI message.
     *
     * @param msg The AgentScope message to convert
     * @return The converted AG-UI message
     */
    public AguiMessage toAguiMessage(Msg msg) {
        String role = convertRole(msg.getRole());
        StringBuilder content = new StringBuilder();
        List<AguiToolCall> toolCalls = new ArrayList<>();
        String toolCallId = null;

        for (ContentBlock block : msg.getContent()) {
            if (block instanceof TextBlock tb) {
                if (!content.isEmpty()) {
                    content.append("\n");
                }
                content.append(tb.getText());
            } else if (block instanceof ToolUseBlock tub) {
                toolCalls.add(toAguiToolCall(tub));
            } else if (block instanceof ToolResultBlock trb) {
                toolCallId = trb.getId();
                // Extract text content from tool result
                for (ContentBlock output : trb.getOutput()) {
                    if (output instanceof TextBlock tb) {
                        if (!content.isEmpty()) {
                            content.append("\n");
                        }
                        content.append(tb.getText());
                    }
                }
            }
        }

        return new AguiMessage(
                msg.getId(),
                role,
                !content.isEmpty() ? content.toString() : null,
                toolCalls.isEmpty() ? null : toolCalls,
                toolCallId);
    }

    /**
     * Convert a list of AG-UI messages to AgentScope messages.
     *
     * @param aguiMessages The AG-UI messages to convert
     * @return The converted AgentScope messages
     */
    public List<Msg> toMsgList(List<AguiMessage> aguiMessages) {
        if (aguiMessages == null || aguiMessages.isEmpty()) {
            return List.of();
        }
        List<Msg> message = aguiMessages.stream().map(this::toMsg).collect(Collectors.toList());

        if ("user".equals(aguiMessages.getLast().getRole())) {
            appendAttachmentReferences(message);
        }

        return message;
    }

    /**
     * Append uploaded attachment references to the latest user message without reading file content.
     */
    private void appendAttachmentReferences(List<Msg> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        Msg latest = messages.getLast();
        if (latest.getRole() != MsgRole.USER) {
            return;
        }
        String content = latest.getTextContent();
        AgentContext agentContext = AgentContext.getIfExists().orElse(null);
        List<UploadedAttachmentRef> refs = UploadedAttachmentReferences.from(agentContext, content);
        if (refs.isEmpty()) {
            return;
        }
        List<ContentBlock> blocks = new ArrayList<>();
        String text = UploadedAttachmentReferences.stripFilePrefix(content);
        if (text != null && !text.isBlank()) {
            blocks.add(TextBlock.builder().text(text).build());
        }
        blocks.add(TextBlock.builder().text(UploadedAttachmentReferences.toPrompt(refs)).build());
        messages.removeLast();
        messages.add(Msg.builder()
                .id(latest.getId())
                .name(latest.getName())
                .role(latest.getRole())
                .content(blocks)
                .metadata(latest.getMetadata())
                .timestamp(latest.getTimestamp())
                .build());
    }

    /**
     * Convert a list of AgentScope messages to AG-UI messages.
     *
     * @param msgs The AgentScope messages to convert
     * @return The converted AG-UI messages
     */
    public List<AguiMessage> toAguiMessageList(List<Msg> msgs) {
        return msgs.stream().map(this::toAguiMessage).collect(Collectors.toList());
    }

    /**
     * Convert an AG-UI role string to an AgentScope MsgRole.
     *
     * @param role The AG-UI role string
     * @return The corresponding MsgRole
     */
    private MsgRole convertRole(String role) {
        return switch (role.toLowerCase()) {
            case "user" -> MsgRole.USER;
            case "assistant" -> MsgRole.ASSISTANT;
            case "system" -> MsgRole.SYSTEM;
            case "tool" -> MsgRole.TOOL;
            default -> MsgRole.USER;
        };
    }

    /**
     * Convert an AgentScope MsgRole to an AG-UI role string.
     *
     * @param role The AgentScope MsgRole
     * @return The corresponding role string
     */
    private String convertRole(MsgRole role) {
        return switch (role) {
            case USER -> "user";
            case ASSISTANT -> "assistant";
            case SYSTEM -> "system";
            case TOOL -> "tool";
        };
    }

    /**
     * Convert an AG-UI tool call to an AgentScope ToolUseBlock.
     *
     * @param tc The AG-UI tool call
     * @return The converted ToolUseBlock
     */
    private ToolUseBlock toToolUseBlock(AguiToolCall tc) {
        Map<String, Object> input = parseJsonArguments(tc.getFunction().getArguments());
        return ToolUseBlock.builder()
                .id(tc.getId())
                .name(tc.getFunction().getName())
                .input(input)
                .build();
    }

    /**
     * Convert an AgentScope ToolUseBlock to an AG-UI tool call.
     *
     * @param tub The AgentScope ToolUseBlock
     * @return The converted AG-UI tool call
     */
    private AguiToolCall toAguiToolCall(ToolUseBlock tub) {
        String arguments = serializeArguments(tub.getInput());
        AguiFunctionCall function = new AguiFunctionCall(tub.getName(), arguments);
        return new AguiToolCall(tub.getId(), function);
    }

    /**
     * Parse JSON arguments string to a Map.
     *
     * @param arguments The JSON arguments string
     * @return The parsed Map
     */
    private Map<String, Object> parseJsonArguments(String arguments) {
        if (arguments == null || arguments.isEmpty()) {
            return Map.of();
        }
        try {
            return JsonUtils.getJsonCodec()
                    .fromJson(arguments, new TypeReference<Map<String, Object>>() {});
        } catch (JsonException e) {
            return Map.of();
        }
    }

    /**
     * Serialize arguments Map to JSON string.
     *
     * @param arguments The arguments Map
     * @return The JSON string
     */
    private String serializeArguments(Map<String, Object> arguments) {
        if (arguments == null || arguments.isEmpty()) {
            return "{}";
        }
        try {
            return JsonUtils.getJsonCodec().toJson(arguments);
        } catch (JsonException e) {
            return "{}";
        }
    }
}
