package com.notfound.timetrackserver.service.impl;

import com.notfound.timetrackcommon.api.ResultCode;
import com.notfound.timetrackcommon.exception.BizException;
import com.notfound.timetrackpojo.entity.MediaEntity;
import com.notfound.timetrackserver.config.StorageProperties;
import com.notfound.timetrackserver.mapper.MediaMapper;
import com.notfound.timetrackserver.service.MediaFileService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class MediaFileServiceImpl implements MediaFileService {

    private final MediaMapper mediaMapper;
    private final StorageProperties storageProperties;

    public MediaFileServiceImpl(MediaMapper mediaMapper, StorageProperties storageProperties) {
        this.mediaMapper = mediaMapper;
        this.storageProperties = storageProperties;
    }

    @Override
    public String previewUrl(Long mediaId, String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {
            return null;
        }
        String value = imagePath.trim();
        if (value.startsWith("http://") || value.startsWith("https://")) {
            return value;
        }
        return "/api/v1/media/" + mediaId + "/file";
    }

    @Override
    public Resource loadMediaFile(Long mediaId) {
        MediaEntity media = mediaMapper.findById(mediaId);
        if (media == null) {
            throw new BizException(ResultCode.NOT_FOUND, "media not found: " + mediaId);
        }
        Path path = resolvePath(media.getImagePath());
        if (!Files.isRegularFile(path) || !Files.isReadable(path)) {
            throw new BizException(ResultCode.NOT_FOUND, "media file not found: " + mediaId);
        }
        try {
            return new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            throw new BizException(ResultCode.INTERNAL_ERROR, "invalid media file path");
        }
    }

    @Override
    public String contentType(Resource resource) {
        try {
            String contentType = Files.probeContentType(resource.getFile().toPath());
            return contentType == null ? "application/octet-stream" : contentType;
        } catch (Exception e) {
            return "application/octet-stream";
        }
    }

    private Path resolvePath(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {
            throw new BizException(ResultCode.NOT_FOUND, "media file path is blank");
        }
        String value = imagePath.trim();
        if (value.startsWith("http://") || value.startsWith("https://")) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "remote media should be accessed by url");
        }
        Path configuredRoot = Path.of(defaultIfBlank(storageProperties.localRootDir(), "storage/uploads"));
        Path root = resolveStorageRoot(configuredRoot);
        if (value.startsWith("/uploads/")) {
            return root.resolve(value.substring("/uploads/".length())).normalize();
        }
        Path path = Path.of(value);
        if (path.isAbsolute()) {
            return path.normalize();
        }
        return root.resolve(value).normalize();
    }

    private Path resolveStorageRoot(Path configuredRoot) {
        if (configuredRoot.isAbsolute()) {
            return configuredRoot.normalize();
        }
        Path cursor = Path.of("").toAbsolutePath().normalize();
        while (cursor != null) {
            Path candidate = cursor.resolve(configuredRoot).normalize();
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
            cursor = cursor.getParent();
        }
        return configuredRoot.toAbsolutePath().normalize();
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
