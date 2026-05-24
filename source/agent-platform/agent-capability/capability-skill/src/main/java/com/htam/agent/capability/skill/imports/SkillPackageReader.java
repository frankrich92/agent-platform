package com.htam.agent.capability.skill.imports;

import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Platform-owned parser for imported skill packages.
 */
public final class SkillPackageReader {

    private static final Pattern FRONTMATTER_FIELD =
            Pattern.compile("(?m)^([A-Za-z0-9_-]+):\\s*(.+?)\\s*$");

    private SkillPackageReader() {
    }

    public static List<String> skillNames(Path skillsDir) {
        return listSkillDirectories(skillsDir).stream()
                .map(SkillPackageReader::readMetadata)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(metadata -> metadata.get("name"))
                .filter(name -> name != null && !name.isBlank())
                .toList();
    }

    public static ImportedSkill read(Path skillsDir, String skillName) {
        Path skillDir = SkillImportInspector.findSkillDirectory(skillsDir, skillName)
                .orElseThrow(() -> new IllegalArgumentException("技能目录不存在: " + skillName));
        Path skillFile = skillDir.resolve(SkillImportConstants.SKILL_FILE);
        String skillContent = readString(skillFile);
        Map<String, String> metadata = parseMetadata(skillContent);
        String name = normalize(metadata.getOrDefault("name", skillName));
        String description = normalize(metadata.getOrDefault("description", ""));
        return new ImportedSkill(name, description, skillContent, readResources(skillDir));
    }

    private static Optional<Map<String, String>> readMetadata(Path skillDir) {
        Path skillFile = skillDir.resolve(SkillImportConstants.SKILL_FILE);
        if (!Files.isRegularFile(skillFile)) {
            return Optional.empty();
        }
        Map<String, String> metadata = parseMetadata(readString(skillFile));
        return metadata.containsKey("name") ? Optional.of(metadata) : Optional.empty();
    }

    private static Map<String, String> parseMetadata(String content) {
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
        Matcher matcher = FRONTMATTER_FIELD.matcher(frontmatter);
        while (matcher.find()) {
            metadata.put(matcher.group(1).trim(), normalize(matcher.group(2)));
        }
        return metadata;
    }

    private static Map<String, String> readResources(Path skillDir) {
        Map<String, String> resources = new LinkedHashMap<>();
        readResourcesUnder(skillDir, "examples", resources);
        readResourcesUnder(skillDir, "references", resources);
        readResourcesUnder(skillDir, "scripts", resources);
        return resources;
    }

    private static void readResourcesUnder(Path skillDir, String resourceDirName, Map<String, String> resources) {
        Path resourceDir = skillDir.resolve(resourceDirName);
        if (!Files.isDirectory(resourceDir)) {
            return;
        }
        try (Stream<Path> files = Files.walk(resourceDir)) {
            files.filter(Files::isRegularFile)
                    .forEach(file -> {
                        Path relative = skillDir.relativize(file);
                        readResourceString(file)
                                .ifPresent(content -> resources.put(relative.toString().replace('\\', '/'), content));
                    });
        } catch (IOException e) {
            throw new IllegalStateException("读取技能资源失败: " + resourceDir, e);
        }
    }

    private static List<Path> listSkillDirectories(Path skillsDir) {
        if (skillsDir == null || !Files.isDirectory(skillsDir)) {
            return List.of();
        }
        try (Stream<Path> entries = Files.list(skillsDir)) {
            return entries.filter(Files::isDirectory)
                    .filter(dir -> !SkillImportConstants.isNoiseDirectory(dir.getFileName().toString()))
                    .toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    private static String readString(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("读取文件失败: " + path, e);
        }
    }

    private static Optional<String> readResourceString(Path path) {
        try {
            return Optional.of(Files.readString(path, StandardCharsets.UTF_8));
        } catch (MalformedInputException e) {
            return Optional.empty();
        } catch (IOException e) {
            throw new IllegalStateException("读取文件失败: " + path, e);
        }
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.trim();
        if (normalized.startsWith("\"") && normalized.endsWith("\"") && normalized.length() >= 2) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }
        return normalized;
    }
}
