package com.htam.agent.runtime.agentscope.skill;

import com.fasterxml.jackson.databind.JsonNode;
import com.htam.agent.common.entity.SkillPackage;
import com.htam.agent.common.skill.SkillMetadata;
import com.htam.agent.repo.capability.SkillPackageRepository;
import com.htam.agent.runtime.core.RuntimeInteractionRecorder;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.message.ToolResultBlock;
import io.agentscope.core.tool.AgentTool;
import io.agentscope.core.tool.ToolCallParam;
import java.util.List;
import java.util.Map;
import reactor.core.publisher.Mono;

public class LoadSkillContentTool implements AgentTool {

    private final SkillPackageRepository skillPackageRepository;

    public LoadSkillContentTool(SkillPackageRepository skillPackageRepository) {
        this.skillPackageRepository = skillPackageRepository;
    }

    @Override
    public String getName() {
        return "load_skill_content";
    }

    @Override
    public String getDescription() {
        return "Load the full content of a bound skill by skill_id when the skill index indicates it is relevant.";
    }

    @Override
    public Map<String, Object> getParameters() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "skill_id", Map.of(
                                "type", "string",
                                "description", "The numeric skill id from the visible skill index")),
                "required", List.of("skill_id"));
    }

    @Override
    public Mono<ToolResultBlock> callAsync(ToolCallParam param) {
        return Mono.fromCallable(() -> {
            String skillId = param.getInput() == null ? null : String.valueOf(param.getInput().get("skill_id"));
            SkillPackage skill = loadSkill(skillId);
            String result = skill == null || !Boolean.TRUE.equals(skill.getEnabled())
                    ? "Skill not found or disabled: " + skillId
                    : fullSkillContent(skill);
            if (skill != null) {
                RuntimeInteractionRecorder.recordSkillLoad(skill.getName(), "skill:" + skill.getId());
            }
            return ToolResultBlock.of(
                    param.getToolUseBlock().getId(),
                    param.getToolUseBlock().getName(),
                    TextBlock.builder().text(result == null ? "" : result).build());
        });
    }

    private SkillPackage loadSkill(String skillId) {
        if (skillId == null || skillId.isBlank()) {
            return null;
        }
        try {
            return skillPackageRepository.getById(Long.valueOf(skillId));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String fullSkillContent(SkillPackage skill) {
        SkillMetadata metadata = SkillMetadata.from(skill);
        StringBuilder content = new StringBuilder();
        content.append("skill_id: ").append(skill.getId()).append('\n');
        content.append("name: ").append(nullToEmpty(skill.getName())).append('\n');
        appendLine(content, "version", metadata.version());
        appendLine(content, "trigger", metadata.trigger());
        if (!metadata.allowedTools().isEmpty()) {
            content.append("allowed_tools: ").append(String.join(", ", metadata.allowedTools())).append('\n');
        }
        appendLine(content, "risk_level", metadata.riskLevel());
        appendLine(content, "source", metadata.source());
        appendLine(content, "summary_hash", metadata.summaryHash());
        content.append('\n').append("--- skill_content ---").append('\n');
        content.append(nullToEmpty(skill.getSkillContent()));
        appendResources(content, "references", skill.getReferences());
        appendResources(content, "examples", skill.getExamples());
        appendResources(content, "scripts", skill.getScripts());
        return content.toString();
    }

    private static void appendResources(StringBuilder content, String title, JsonNode resources) {
        if (resources == null || resources.isEmpty()) {
            return;
        }
        content.append('\n').append("--- ").append(title).append(" ---").append('\n');
        for (JsonNode resource : resources) {
            String prefix = text(resource, "prefix", title);
            String name = text(resource, "name", "unnamed");
            String body = text(resource, "content", "");
            content.append('[').append(prefix).append('/').append(name).append(']').append('\n');
            content.append(body).append('\n');
        }
    }

    private static String text(JsonNode node, String field, String fallback) {
        if (node == null || !node.has(field) || node.get(field).isNull()) {
            return fallback;
        }
        return node.get(field).asText(fallback);
    }

    private static void appendLine(StringBuilder content, String key, String value) {
        if (value != null && !value.isBlank()) {
            content.append(key).append(": ").append(value).append('\n');
        }
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
