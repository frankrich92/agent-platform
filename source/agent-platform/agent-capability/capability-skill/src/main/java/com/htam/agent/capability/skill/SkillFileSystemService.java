package com.htam.agent.capability.skill;

import com.htam.agent.common.consts.SysConst;
import com.htam.agent.common.enums.SkillFileType;
import com.htam.agent.common.util.FolderUtils;
import com.htam.agent.governance.system.params.core.ParamsAdapter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class SkillFileSystemService {
    private static final String BASE_DIR = SysConst.SKILLS_DIR;
    private static final List<String> DB_DIR_PREFIXES = List.of("references/", "examples/", "scripts/");
    private static final String SKILL_MD_NAME = "SKILL.md";
    private static final String EXT_PARAMS_KEY = "SKILL_FILE_ALLOWED_EXTENSIONS";
    private static final Set<String> DEFAULT_ALLOWED_EXTENSIONS = Set.of(
            "md", "py", "sh", "js", "ts", "json", "yaml", "yml", "xml", "txt",
            "java", "cs", "go", "rs", "rb", "php", "sql", "html", "css", "scss", "less", "cfg", "conf", "toml");

    private static volatile ParamsAdapter paramsAdapter;
    private static volatile Set<String> cachedExtensions;

    public static void setParamsAdapter(ParamsAdapter adapter) {
        paramsAdapter = adapter;
    }

    public static void clearExtensionCache() {
        cachedExtensions = null;
        log.info("技能文件扩展名白名单缓存已清除");
    }

    public static boolean shouldPersistToDb(String relativePath) {
        String normalizedPath = normalizeRelativePath(relativePath);
        if (normalizedPath == null) {
            return false;
        }
        if (SKILL_MD_NAME.equals(normalizedPath)) {
            return true;
        }
        for (String prefix : DB_DIR_PREFIXES) {
            if (normalizedPath.startsWith(prefix)) {
                return isAllowedExtension(normalizedPath);
            }
        }
        return false;
    }

    public static List<String> getAllowedExtensions() {
        return new ArrayList<>(loadAllowedExtensions());
    }

    public static SkillFileType resolveFileType(String relativePath) {
        String normalizedPath = normalizeRelativePath(relativePath);
        if (normalizedPath == null) {
            return null;
        }
        if (SKILL_MD_NAME.equals(normalizedPath)) {
            return SkillFileType.SKILL_MD;
        }
        if (normalizedPath.startsWith("references/")) {
            return SkillFileType.REFERENCES;
        }
        if (normalizedPath.startsWith("examples/")) {
            return SkillFileType.EXAMPLES;
        }
        if (normalizedPath.startsWith("scripts/")) {
            return SkillFileType.SCRIPTS;
        }
        return null;
    }

    public static Path getSkillDirPath(String skillName) {
        return Paths.get(BASE_DIR, skillName);
    }

    public static Path buildSkillDir(String skillName) {
        Path skillDir = getSkillDirPath(skillName);
        FolderUtils.mkdirsByAbsolutePath(skillDir.toAbsolutePath().toString());
        return skillDir;
    }

    public static boolean writeFile(String skillName, String relativePath, String content) {
        return writeFileBytes(skillName, relativePath, (content == null ? "" : content).getBytes(StandardCharsets.UTF_8));
    }

    public static boolean writeFileBytes(String skillName, String relativePath, byte[] bytes) {
        try {
            Path filePath = resolveSafePath(buildSkillDir(skillName), relativePath);
            Path parentDir = filePath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
            Files.write(filePath, bytes == null ? new byte[0] : bytes);
            return true;
        } catch (IOException | IllegalArgumentException e) {
            log.error("写入技能文件失败: skillName={}, path={}", skillName, relativePath, e);
            return false;
        }
    }

    public static boolean deleteFile(String skillName, String relativePath) {
        try {
            Files.deleteIfExists(resolveSafePath(getSkillDirPath(skillName), relativePath));
            return true;
        } catch (IOException | IllegalArgumentException e) {
            log.error("删除技能文件失败: skillName={}, path={}", skillName, relativePath, e);
            return false;
        }
    }

    public static boolean deleteDirectory(String skillName, String relativePath) {
        Path dirPath = resolveSafePath(getSkillDirPath(skillName), relativePath);
        return FolderUtils.deleteRecursively(dirPath.toAbsolutePath().toString());
    }

    public static boolean createDirectory(String skillName, String relativePath) {
        Path dirPath = resolveSafePath(buildSkillDir(skillName), relativePath);
        FolderUtils.mkdirsByAbsolutePath(dirPath.toAbsolutePath().toString());
        return true;
    }

    public static boolean removeSkillDir(String skillName) {
        Path skillDir = getSkillDirPath(skillName);
        if (Files.exists(skillDir)) {
            return FolderUtils.deleteRecursively(skillDir.toAbsolutePath().toString());
        }
        return true;
    }

    public static List<FileTreeNode> scanSkillTree(String skillName) {
        Path skillDir = getSkillDirPath(skillName);
        if (!Files.exists(skillDir)) {
            return Collections.emptyList();
        }
        Map<String, FileTreeNode> dirMap = new LinkedHashMap<>();
        List<FileTreeNode> rootNodes = new ArrayList<>();
        try {
            Files.walkFileTree(skillDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (dir.equals(skillDir)) {
                        return FileVisitResult.CONTINUE;
                    }
                    String relPath = skillDir.relativize(dir).toString().replace('\\', '/');
                    FileTreeNode node = new FileTreeNode(dir.getFileName().toString(), relPath, true);
                    addNode(rootNodes, dirMap, node);
                    dirMap.put(relPath, node);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    String relPath = skillDir.relativize(file).toString().replace('\\', '/');
                    FileTreeNode node = new FileTreeNode(file.getFileName().toString(), relPath, false);
                    node.setExtension(getExtension(file.getFileName().toString()));
                    node.setFileSize(attrs.size());
                    addNode(rootNodes, dirMap, node);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.error("扫描技能包文件树失败: skillName={}", skillName, e);
        }
        sortTreeNodes(rootNodes);
        return rootNodes;
    }

    public static String readFileContent(String skillName, String relativePath) {
        try {
            Path filePath = resolveSafePath(getSkillDirPath(skillName), relativePath);
            if (!Files.exists(filePath)) {
                return null;
            }
            return Files.readString(filePath, StandardCharsets.UTF_8);
        } catch (IOException | IllegalArgumentException e) {
            log.error("读取技能文件内容失败: skillName={}, path={}", skillName, relativePath, e);
            return null;
        }
    }

    public static Map<String, String> parseSkillMdHeader(String content) {
        Map<String, String> result = new HashMap<>();
        if (content == null || content.isEmpty()) {
            return result;
        }
        String[] lines = content.split("\\n");
        boolean inHeader = false;
        int separatorCount = 0;
        for (String line : lines) {
            if ("---".equals(line.trim())) {
                separatorCount++;
                if (separatorCount == 1) {
                    inHeader = true;
                    continue;
                } else if (inHeader) {
                    break;
                }
            }
            if (inHeader) {
                int colonIdx = line.indexOf(':');
                if (colonIdx > 0) {
                    result.put(line.substring(0, colonIdx).trim(), line.substring(colonIdx + 1).trim());
                }
            }
        }
        return result;
    }

    public static String buildSkillMdContent(String name, String description) {
        return "---\n"
                + "name: " + (name != null ? name : "") + "\n"
                + "description: " + (description != null ? description : "") + "\n"
                + "---\n";
    }

    public static String normalizeRelativePath(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return null;
        }
        String normalized = relativePath.replace('\\', '/');
        try {
            Path path = Paths.get(normalized).normalize();
            if (path.isAbsolute() || path.startsWith("..")) {
                return null;
            }
            return path.toString().replace('\\', '/');
        } catch (InvalidPathException e) {
            return null;
        }
    }

    private static void addNode(List<FileTreeNode> rootNodes, Map<String, FileTreeNode> dirMap, FileTreeNode node) {
        String parentPath = getParentPath(node.getPath());
        FileTreeNode parentNode = dirMap.get(parentPath);
        if (parentNode != null) {
            parentNode.getChildren().add(node);
        } else {
            rootNodes.add(node);
        }
    }

    private static Path resolveSafePath(Path root, String relativePath) {
        String normalizedPath = normalizeRelativePath(relativePath);
        if (normalizedPath == null) {
            throw new IllegalArgumentException("非法技能文件路径: " + relativePath);
        }
        Path normalizedRoot = root.toAbsolutePath().normalize();
        Path resolved = normalizedRoot.resolve(normalizedPath).normalize();
        if (!resolved.startsWith(normalizedRoot)) {
            throw new IllegalArgumentException("非法技能文件路径: " + relativePath);
        }
        return resolved;
    }

    private static Set<String> loadAllowedExtensions() {
        Set<String> cached = cachedExtensions;
        if (cached != null) {
            return cached;
        }
        if (paramsAdapter != null) {
            String value = paramsAdapter.getValue(EXT_PARAMS_KEY);
            if (value != null && !value.isEmpty()) {
                Set<String> result = Arrays.stream(value.split(","))
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toSet());
                cachedExtensions = result;
                return result;
            }
        }
        return DEFAULT_ALLOWED_EXTENSIONS;
    }

    private static boolean isAllowedExtension(String path) {
        int dotIdx = path.lastIndexOf('.');
        if (dotIdx < 0) {
            return false;
        }
        return loadAllowedExtensions().contains(path.substring(dotIdx + 1).toLowerCase());
    }

    private static void sortTreeNodes(List<FileTreeNode> nodes) {
        nodes.sort(Comparator.comparing(FileTreeNode::isDirectory).reversed()
                .thenComparing(FileTreeNode::getName, String.CASE_INSENSITIVE_ORDER));
        for (FileTreeNode node : nodes) {
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                sortTreeNodes(node.getChildren());
            }
        }
    }

    private static String getParentPath(String path) {
        int lastSlash = path.lastIndexOf('/');
        return lastSlash <= 0 ? "" : path.substring(0, lastSlash);
    }

    private static String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 && dotIndex < fileName.length() - 1
                ? fileName.substring(dotIndex + 1).toLowerCase()
                : "";
    }

    public static class FileTreeNode {
        private String name;
        private String path;
        private boolean directory;
        private String extension = "";
        private long fileSize;
        private List<FileTreeNode> children = new ArrayList<>();

        public FileTreeNode() {
        }

        public FileTreeNode(String name, String path, boolean directory) {
            this.name = name;
            this.path = path;
            this.directory = directory;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        public boolean isDirectory() { return directory; }
        public void setDirectory(boolean directory) { this.directory = directory; }
        public String getExtension() { return extension; }
        public void setExtension(String extension) { this.extension = extension; }
        public long getFileSize() { return fileSize; }
        public void setFileSize(long fileSize) { this.fileSize = fileSize; }
        public List<FileTreeNode> getChildren() { return children; }
        public void setChildren(List<FileTreeNode> children) { this.children = children; }
    }
}
