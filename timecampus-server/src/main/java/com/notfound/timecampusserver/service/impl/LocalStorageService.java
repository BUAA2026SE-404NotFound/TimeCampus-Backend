package com.notfound.timecampusserver.service.impl;

import com.notfound.timecampuscommon.api.ResultCode;
import com.notfound.timecampuscommon.exception.BizException;
import com.notfound.timecampusserver.config.StorageProperties;
import com.notfound.timecampusserver.service.StorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

@Service
public class LocalStorageService implements StorageService {

    static final String DEFAULT_STORAGE_ROOT = "/home/ubuntu/cos";

    private final StorageProperties properties;

    public LocalStorageService(StorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public String store(MultipartFile file, Long userId) {
        validate(file);
        String ext = extension(file);
        String date = LocalDate.now().toString();
        String filename = userId + "-" + UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path root = storageRoot();
        Path targetDir = root.resolve(date).normalize();
        Path target = targetDir.resolve(filename).normalize();
        if (!target.startsWith(root)) {
            throw new BizException(ResultCode.INTERNAL_ERROR, "resolved storage path escapes root");
        }
        try {
            Files.createDirectories(targetDir);
            file.transferTo(target);
        } catch (IOException e) {
            throw new BizException(ResultCode.INTERNAL_ERROR, "failed to store file: " + e.getMessage());
        }
        return target.toString();
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "file is required");
        }
        long maxMb = properties.maxFileSizeMb() == null ? 10 : properties.maxFileSizeMb();
        if (file.getSize() > maxMb * 1024 * 1024) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "image size must be <= " + maxMb + "MB");
        }
        String ext = extension(file);
        if (!"jpg".equals(ext) && !"jpeg".equals(ext) && !"png".equals(ext)) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "only jpg/png images are allowed");
        }
    }

    private String extension(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.contains(".")) {
            String contentType = file.getContentType();
            if ("image/jpeg".equalsIgnoreCase(contentType)) {
                return "jpg";
            }
            if ("image/png".equalsIgnoreCase(contentType)) {
                return "png";
            }
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private Path storageRoot() {
        String configuredRoot = properties == null ? null : properties.localRootDir();
        return Path.of(defaultIfBlank(configuredRoot, DEFAULT_STORAGE_ROOT))
                .toAbsolutePath()
                .normalize();
    }
}
