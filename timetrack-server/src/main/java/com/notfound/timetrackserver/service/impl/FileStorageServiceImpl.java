package com.notfound.timetrackserver.service.impl;


import com.notfound.timetrackcommon.api.ResultCode;
import com.notfound.timetrackcommon.exception.BizException;
import com.notfound.timetrackserver.config.StorageProperties;
import com.notfound.timetrackserver.service.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path uploadRoot;

    public FileStorageServiceImpl(StorageProperties storageProperties) {
        String rootDir = storageProperties.localRootDir();
        if (rootDir == null || rootDir.isBlank()) {
            rootDir = "./uploads";
        }
        this.uploadRoot = Paths.get(rootDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadRoot);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    @Override
    public String saveUgcImage(Long userId, MultipartFile file) {
        // 校验文件非空
        if (file == null || file.isEmpty()) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "上传文件不能为空");
        }

        // 校验文件类型（简单白名单）
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/") &&
                !contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "仅支持 JPEG/PNG 格式图片");
        }

        // 生成存储路径：uploads/ugc/{userId}/{timestamp}_{uuid}.jpg
        String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Path userDir = uploadRoot.resolve("ugc").resolve(String.valueOf(userId)).resolve(dateDir);
        try {
            Files.createDirectories(userDir);
        } catch (IOException e) {
            throw new BizException(ResultCode.INTERNAL_ERROR, "创建目录失败");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        } else if (contentType.contains("jpeg")) {
            extension = ".jpg";
        } else if (contentType.contains("png")) {
            extension = ".png";
        } else {
            extension = ".img";
        }
        String newFileName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
        Path targetPath = userDir.resolve(newFileName);

        try {
            file.transferTo(targetPath.toFile());
        } catch (IOException e) {
            throw new BizException(ResultCode.INTERNAL_ERROR, "文件保存失败");
        }

        // 返回相对路径（前端可通过配置的静态资源映射访问）
        return "/uploads/ugc/" + userId + "/" + dateDir + "/" + newFileName;
    }
}
