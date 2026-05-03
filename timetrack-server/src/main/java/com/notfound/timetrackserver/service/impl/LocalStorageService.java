package com.notfound.timetrackserver.service.impl;

import com.notfound.timetrackcommon.api.ResultCode;
import com.notfound.timetrackcommon.exception.BizException;
import com.notfound.timetrackserver.config.StorageProperties;
import com.notfound.timetrackserver.service.StorageService;
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

    private final StorageProperties properties;

    public LocalStorageService(StorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public String store(MultipartFile file, Long userId) {
        validate(file);
        String ext = extension(file.getOriginalFilename());
        String date = LocalDate.now().toString();
        String filename = userId + "-" + UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path root = Path.of(defaultIfBlank(properties.localRootDir(), "storage/uploads"));
        Path targetDir = root.resolve(date);
        Path target = targetDir.resolve(filename).normalize();
        try {
            Files.createDirectories(targetDir);
            file.transferTo(target);
        } catch (IOException e) {
            throw new BizException(ResultCode.INTERNAL_ERROR, "failed to store file: " + e.getMessage());
        }
        return "/uploads/" + date + "/" + filename;
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "file is required");
        }
        long maxMb = properties.maxFileSizeMb() == null ? 10 : properties.maxFileSizeMb();
        if (file.getSize() > maxMb * 1024 * 1024) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "image size must be <= " + maxMb + "MB");
        }
        String ext = extension(file.getOriginalFilename());
        if (!"jpg".equals(ext) && !"jpeg".equals(ext) && !"png".equals(ext)) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "only jpg/png images are allowed");
        }
    }

    private String extension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
