package com.htam.agent.common.skill;

import com.htam.agent.common.entity.SkillPackage;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record SkillMetadata(
        String version,
        String trigger,
        List<String> allowedTools,
        String riskLevel,
        String source,
        String summaryHash) {

    public SkillMetadata {
        version = normalize(version);
        trigger = normalize(trigger);
        allowedTools = allowedTools == null
                ? List.of()
                : allowedTools.stream()
                        .map(SkillMetadata::normalize)
                        .filter(value -> !value.isBlank())
                        .distinct()
                        .toList();
        riskLevel = normalize(riskLevel).toUpperCase();
        source = normalize(source);
        summaryHash = normalize(summaryHash);
    }

    public static SkillMetadata from(SkillPackage skillPackage) {
        String content = skillPackage == null ? null : skillPackage.getSkillContent();
        Map<String, String> metadata = parseFrontmatter(content);
        return new SkillMetadata(
                metadata.getOrDefault("version", ""),
                firstNonBlank(metadata.get("trigger"), metadata.get("triggers"), metadata.get("when")),
                splitList(firstNonBlank(
                        metadata.get("allowed_tools"),
                        metadata.get("allowedTools"),
                        metadata.get("tools"))),
                firstNonBlank(metadata.get("risk_level"), metadata.get("risk"), metadata.get("riskLevel")),
                metadata.getOrDefault("source", ""),
                firstNonBlank(
                        metadata.get("summary_hash"),
                        metadata.get("content_hash"),
                        metadata.get("hash"),
                        sha256(content)));
    }

    public Map<String, Object> asAttributes() {
        Map<String, Object> attributes = new LinkedHashMap<>();
        put(attributes, "version", version);
        put(attributes, "trigger", trigger);
        if (!allowedTools.isEmpty()) {
            attributes.put("allowedTools", allowedTools);
        }
        put(attributes, "riskLevel", riskLevel);
        put(attributes, "source", source);
        put(attributes, "summaryHash", summaryHash);
        return attributes;
    }

    private static Map<String, String> parseFrontmatter(String content) {
        Map<String, String> metadata = new LinkedHashMap<>();
        if (content == null || content.isBlank()) {
            return metadata;
        }
        String normalized = content.replace("\r\n", "\n");
        if (!normalized.startsWith("---")) {
            return metadata;
        }
        int end = normalized.indexOf("\n---", 3);
        if (end < 0) {
            return metadata;
        }
        String frontmatter = normalized.substring(3, end);
        for (String line : frontmatter.split("\n")) {
            int separator = line.indexOf(':');
            if (separator <= 0) {
                continue;
            }
            String key = line.substring(0, separator).trim();
            String value = normalize(line.substring(separator + 1));
            if (!key.isBlank() && !value.isBlank()) {
                metadata.put(key, value);
            }
        }
        return metadata;
    }

    private static List<String> splitList(String value) {
        String normalized = normalize(value);
        if (normalized.isBlank()) {
            return List.of();
        }
        if (normalized.startsWith("[") && normalized.endsWith("]") && normalized.length() >= 2) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }
        return Arrays.stream(normalized.split(","))
                .map(SkillMetadata::normalize)
                .filter(item -> !item.isBlank())
                .distinct()
                .toList();
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            String normalized = normalize(value);
            if (!normalized.isBlank()) {
                return normalized;
            }
        }
        return "";
    }

    private static void put(Map<String, Object> attributes, String key, String value) {
        if (value != null && !value.isBlank()) {
            attributes.put(key, value);
        }
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.trim();
        if (normalized.length() >= 2
                && ((normalized.startsWith("\"") && normalized.endsWith("\""))
                || (normalized.startsWith("'") && normalized.endsWith("'")))) {
            normalized = normalized.substring(1, normalized.length() - 1).trim();
        }
        return normalized;
    }

    private static String sha256(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                hash.append(String.format("%02x", b));
            }
            return hash.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 digest is unavailable", e);
        }
    }
}
