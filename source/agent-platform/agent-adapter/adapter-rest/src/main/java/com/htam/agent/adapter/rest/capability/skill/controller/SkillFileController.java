package com.htam.agent.adapter.rest.capability.skill.controller;

import com.htam.agent.capability.skill.SkillFileSystemService;
import com.htam.agent.capability.skill.service.SkillFileService;
import com.htam.agent.capability.skill.service.SkillPackageService;
import com.htam.agent.common.config.auth.RoleNeed;
import com.htam.agent.common.entity.SkillFile;
import com.htam.agent.common.entity.SkillPackage;
import com.htam.agent.common.enums.Role;
import com.htam.agent.common.enums.SkillFileType;
import com.htam.agent.common.exception.BusinessException;
import com.htam.agent.common.r.R;
import com.htam.agent.common.vo.SkillFileTreeNodeVO;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/skill")
@RequiredArgsConstructor
public class SkillFileController {
    private final SkillPackageService skillPackageService;
    private final SkillFileService skillFileService;

    @GetMapping("/{skillId}/tree")
    public R<List<SkillFileTreeNodeVO>> getTree(@PathVariable("skillId") Long skillId) {
        SkillPackage skillPackage = requireSkill(skillId);
        Map<String, SkillFile> dbFileMap = new HashMap<>();
        for (SkillFile file : skillFileService.listBySkillId(skillId)) {
            dbFileMap.put(file.getFilePath(), file);
        }
        return R.data(convertToVo(SkillFileSystemService.scanSkillTree(skillPackage.getName()), dbFileMap));
    }

    @PostMapping("/{skillId}/files")
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<SkillFileTreeNodeVO> createFile(@PathVariable("skillId") Long skillId,
                                             @RequestBody CreateFileRequest request) {
        SkillPackage skillPackage = requireSkill(skillId);
        String relativePath = joinPath(request.getParentPath(), request.getFileName());
        String content = request.getContent() == null ? "" : request.getContent();
        SkillFileSystemService.writeFile(skillPackage.getName(), relativePath, content);

        SkillFileTreeNodeVO vo = fileNode(request.getFileName(), relativePath);
        if (SkillFileSystemService.shouldPersistToDb(relativePath)) {
            SkillFile file = new SkillFile();
            file.setSkillId(skillId);
            file.setFileType(SkillFileSystemService.resolveFileType(relativePath));
            file.setFileName(request.getFileName());
            file.setFilePath(relativePath);
            file.setContent(content);
            file.setSort(0);
            skillFileService.save(file);
            vo.setFileId(file.getId());
            vo.setFileType(file.getFileType().name());
        }
        return R.data(vo);
    }

    @PutMapping("/files/{fileId}")
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<Boolean> updateFile(@PathVariable("fileId") Long fileId, @RequestBody UpdateFileRequest request) {
        SkillFile file = skillFileService.getById(fileId);
        if (file == null) {
            throw new BusinessException("文件不存在");
        }
        SkillPackage skillPackage = requireSkill(file.getSkillId());
        String content = request.getContent() == null ? "" : request.getContent();
        skillFileService.updateContent(fileId, content);
        SkillFileSystemService.writeFile(skillPackage.getName(), file.getFilePath(), content);
        if (file.getFileType() == SkillFileType.SKILL_MD) {
            syncSkillMetadata(skillPackage, content);
        }
        return R.data(true);
    }

    @PutMapping("/{skillId}/filesystem-write")
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<Boolean> writeFileSystemFile(@PathVariable("skillId") Long skillId,
                                          @RequestBody WriteFileSystemRequest request) {
        SkillPackage skillPackage = requireSkill(skillId);
        String path = normalizeRequired(request.getPath());
        String content = request.getContent() == null ? "" : request.getContent();
        SkillFileSystemService.writeFile(skillPackage.getName(), path, content);
        if (SkillFileSystemService.shouldPersistToDb(path)) {
            upsertDbFile(skillId, path, content);
        }
        return R.data(true);
    }

    @DeleteMapping("/files/{fileId}")
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<Boolean> deleteDbFile(@PathVariable("fileId") Long fileId) {
        SkillFile file = skillFileService.getById(fileId);
        if (file == null) {
            return R.data(true);
        }
        if ("SKILL.md".equalsIgnoreCase(file.getFileName())) {
            throw new BusinessException("SKILL.md 不允许删除");
        }
        SkillPackage skillPackage = requireSkill(file.getSkillId());
        skillFileService.deleteById(fileId);
        SkillFileSystemService.deleteFile(skillPackage.getName(), file.getFilePath());
        return R.data(true);
    }

    @DeleteMapping("/{skillId}/filesystem")
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<Boolean> deleteFileSystemNode(@PathVariable("skillId") Long skillId,
                                           @RequestBody DeleteFileSystemRequest request) {
        SkillPackage skillPackage = requireSkill(skillId);
        String path = normalizeRequired(request.getPath());
        boolean result = request.isDirectory()
                ? SkillFileSystemService.deleteDirectory(skillPackage.getName(), path)
                : SkillFileSystemService.deleteFile(skillPackage.getName(), path);
        if (request.isDirectory()) {
            skillFileService.removeBySkillIdAndPathPrefix(skillId, path.endsWith("/") ? path : path + "/");
        } else {
            skillFileService.removeBySkillIdAndPath(skillId, path);
        }
        return R.data(result);
    }

    @PostMapping("/{skillId}/directories")
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<Boolean> createDirectory(@PathVariable("skillId") Long skillId,
                                      @RequestBody CreateDirectoryRequest request) {
        SkillPackage skillPackage = requireSkill(skillId);
        return R.data(SkillFileSystemService.createDirectory(
                skillPackage.getName(), joinPath(request.getParentPath(), request.getDirName())));
    }

    @PostMapping("/{skillId}/upload")
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<SkillFileTreeNodeVO> uploadFile(@PathVariable("skillId") Long skillId,
                                             @RequestParam("parentPath") String parentPath,
                                             @RequestParam("file") MultipartFile file) throws Exception {
        SkillPackage skillPackage = requireSkill(skillId);
        String relativePath = joinPath(parentPath, file.getOriginalFilename());
        byte[] bytes = file.getBytes();
        SkillFileSystemService.writeFileBytes(skillPackage.getName(), relativePath, bytes);
        SkillFileTreeNodeVO vo = fileNode(file.getOriginalFilename(), relativePath);
        if (SkillFileSystemService.shouldPersistToDb(relativePath)) {
            SkillFile dbFile = upsertDbFile(skillId, relativePath, new String(bytes, StandardCharsets.UTF_8));
            vo.setFileId(dbFile.getId());
            vo.setFileType(dbFile.getFileType().name());
        }
        vo.setFileSize(bytes.length);
        return R.data(vo);
    }

    @GetMapping("/{skillId}/file-content")
    public R<String> getFileContent(@PathVariable("skillId") Long skillId, @RequestParam("path") String path) {
        SkillPackage skillPackage = requireSkill(skillId);
        String content = SkillFileSystemService.readFileContent(skillPackage.getName(), path);
        return R.data(content == null ? "" : content);
    }

    @GetMapping("/allowed-extensions")
    public R<List<String>> getAllowedExtensions() {
        return R.data(SkillFileSystemService.getAllowedExtensions());
    }

    @GetMapping("/{skillId}/download")
    public void downloadFile(@PathVariable("skillId") Long skillId,
                             @RequestParam("path") String path,
                             HttpServletResponse response) throws IOException {
        SkillPackage skillPackage = requireSkill(skillId);
        Path skillDir = SkillFileSystemService.getSkillDirPath(skillPackage.getName()).toAbsolutePath().normalize();
        Path filePath = resolveDownloadPath(skillDir, path);
        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            throw new BusinessException("文件不存在");
        }

        String fileName = filePath.getFileName().toString();
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", contentDisposition(fileName));
        response.setContentLengthLong(Files.size(filePath));

        try (OutputStream out = response.getOutputStream(); InputStream in = Files.newInputStream(filePath)) {
            in.transferTo(out);
            out.flush();
        }
    }

    @GetMapping("/{skillId}/download-zip")
    public void downloadZip(@PathVariable("skillId") Long skillId, HttpServletResponse response) throws IOException {
        SkillPackage skillPackage = requireSkill(skillId);
        Path skillDir = SkillFileSystemService.getSkillDirPath(skillPackage.getName()).toAbsolutePath().normalize();
        if (!Files.exists(skillDir) || !Files.isDirectory(skillDir)) {
            throw new BusinessException("技能包目录不存在");
        }

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", contentDisposition(skillPackage.getName() + ".zip"));

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream());
             var paths = Files.walk(skillDir)) {
            for (Path filePath : paths.filter(Files::isRegularFile).toList()) {
                String entryName = skillDir.relativize(filePath).toString().replace('\\', '/');
                zipOutputStream.putNextEntry(new ZipEntry(entryName));
                Files.copy(filePath, zipOutputStream);
                zipOutputStream.closeEntry();
            }
            zipOutputStream.flush();
        }
    }

    private SkillPackage requireSkill(Long skillId) {
        SkillPackage skillPackage = skillPackageService.getById(skillId);
        if (skillPackage == null) {
            throw new BusinessException("技能包不存在");
        }
        return skillPackage;
    }

    private void syncSkillMetadata(SkillPackage skillPackage, String content) {
        Map<String, String> header = SkillFileSystemService.parseSkillMdHeader(content);
        String newName = header.get("name");
        String newDescription = header.get("description");
        boolean needUpdate = true;
        skillPackage.setSkillContent(content);
        if (newName != null && !newName.isEmpty() && !newName.equals(skillPackage.getName())) {
            skillPackage.setName(newName);
            needUpdate = true;
        }
        if (newDescription != null && !newDescription.equals(skillPackage.getDescription())) {
            skillPackage.setDescription(newDescription);
            needUpdate = true;
        }
        if (needUpdate) {
            skillPackageService.updateById(skillPackage);
        }
    }

    private SkillFile upsertDbFile(Long skillId, String path, String content) {
        String normalized = normalizeRequired(path);
        SkillFile existing = skillFileService.getBySkillIdAndPath(skillId, normalized);
        SkillFile file = existing == null ? new SkillFile() : existing;
        file.setSkillId(skillId);
        file.setFileType(SkillFileSystemService.resolveFileType(normalized));
        file.setFileName(fileName(normalized));
        file.setFilePath(normalized);
        file.setContent(content == null ? "" : content);
        file.setSort(0);
        if (existing == null) {
            skillFileService.save(file);
        } else {
            skillFileService.updateById(file);
        }
        return file;
    }

    private List<SkillFileTreeNodeVO> convertToVo(List<SkillFileSystemService.FileTreeNode> fsNodes,
                                                  Map<String, SkillFile> dbFileMap) {
        return fsNodes.stream().map(fsNode -> {
            SkillFileTreeNodeVO vo = new SkillFileTreeNodeVO();
            vo.setName(fsNode.getName());
            vo.setPath(fsNode.getPath());
            vo.setDirectory(fsNode.isDirectory());
            vo.setExtension(fsNode.getExtension());
            vo.setFileSize(fsNode.getFileSize());
            SkillFile dbFile = dbFileMap.get(fsNode.getPath());
            if (dbFile != null) {
                vo.setFileId(dbFile.getId());
                vo.setFileType(dbFile.getFileType().name());
            }
            if (!fsNode.getChildren().isEmpty()) {
                vo.setChildren(convertToVo(fsNode.getChildren(), dbFileMap));
            }
            return vo;
        }).toList();
    }

    private SkillFileTreeNodeVO fileNode(String fileName, String relativePath) {
        SkillFileTreeNodeVO vo = new SkillFileTreeNodeVO();
        vo.setName(fileName);
        vo.setPath(relativePath);
        vo.setDirectory(false);
        vo.setExtension(extension(fileName));
        return vo;
    }

    private String joinPath(String parentPath, String name) {
        String safeName = normalizeRequired(name);
        if (safeName.contains("/")) {
            throw new BusinessException("文件名不能包含路径分隔符");
        }
        String parent = SkillFileSystemService.normalizeRelativePath(parentPath);
        String path = parent == null ? safeName : parent + "/" + safeName;
        return normalizeRequired(path);
    }

    private String normalizeRequired(String path) {
        String normalized = SkillFileSystemService.normalizeRelativePath(path);
        if (normalized == null) {
            throw new BusinessException("非法技能文件路径");
        }
        return normalized;
    }

    private String fileName(String path) {
        int slash = path.lastIndexOf('/');
        return slash >= 0 ? path.substring(slash + 1) : path;
    }

    private String extension(String fileName) {
        int dotIndex = fileName == null ? -1 : fileName.lastIndexOf('.');
        return dotIndex > 0 && dotIndex < fileName.length() - 1
                ? fileName.substring(dotIndex + 1).toLowerCase()
                : "";
    }

    private Path resolveDownloadPath(Path skillDir, String path) {
        String normalizedPath = SkillFileSystemService.normalizeRelativePath(path);
        if (normalizedPath == null) {
            throw new BusinessException("非法技能文件路径");
        }
        Path filePath = skillDir.resolve(normalizedPath).normalize();
        if (!filePath.startsWith(skillDir)) {
            throw new BusinessException("非法技能文件路径");
        }
        return filePath;
    }

    private String contentDisposition(String fileName) {
        return "attachment; filename=\"" + new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1) + "\"";
    }

    @Data
    public static class CreateFileRequest {
        private String parentPath;
        private String fileName;
        private String content;
    }

    @Data
    public static class UpdateFileRequest {
        private String content;
    }

    @Data
    public static class WriteFileSystemRequest {
        private String path;
        private String content;
    }

    @Data
    public static class DeleteFileSystemRequest {
        private String path;
        private boolean directory;
    }

    @Data
    public static class CreateDirectoryRequest {
        private String parentPath;
        private String dirName;
    }
}
