package com.notfound.timecampusserver.service.impl;

import com.notfound.timecampuscommon.api.ResultCode;
import com.notfound.timecampuscommon.exception.BizException;
import com.notfound.timecampuspojo.entity.MediaEntity;
import com.notfound.timecampusserver.config.StorageProperties;
import com.notfound.timecampusserver.mapper.MediaMapper;
import com.notfound.timecampusserver.service.MediaFileService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;

@Service
public class MediaFileServiceImpl implements MediaFileService {

    private static final String DEFAULT_STORAGE_ROOT = LocalStorageService.DEFAULT_STORAGE_ROOT;
    private static final String MEDIA_FILE_TOKEN_PREFIX = "media:file:token:";
    private static final String ADMIN_MEDIA_FILE_TOKEN_PREFIX = "admin:media:file:token:";
    private static final long DEFAULT_MEDIA_FILE_TOKEN_TTL_SECONDS = 600L;

    private final MediaMapper mediaMapper;
    private final StorageProperties storageProperties;
    private final StringRedisTemplate redisTemplate;

    public MediaFileServiceImpl(MediaMapper mediaMapper,
                                StorageProperties storageProperties,
                                StringRedisTemplate redisTemplate) {
        this.mediaMapper = mediaMapper;
        this.storageProperties = storageProperties;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public String previewUrl(Long mediaId, String imagePath) {
        return signedPreviewUrl(mediaId, imagePath, "/api/v1/media/" + mediaId + "/file", MEDIA_FILE_TOKEN_PREFIX);
    }

    @Override
    public String adminPreviewUrl(Long mediaId, String imagePath) {
        return signedPreviewUrl(mediaId, imagePath, "/api/v1/admin/media/" + mediaId + "/file", ADMIN_MEDIA_FILE_TOKEN_PREFIX);
    }

    private String signedPreviewUrl(Long mediaId, String imagePath, String path, String tokenPrefix) {
        if (imagePath == null || imagePath.isBlank()) {
            return null;
        }
        String value = imagePath.trim();
        if (value.startsWith("http://") || value.startsWith("https://")) {
            return value;
        }
        String accessToken = issueAccessToken(mediaId, tokenPrefix);
        try {
            return ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path(path)
                    .queryParam("accessToken", accessToken)
                    .toUriString();
        } catch (IllegalStateException e) {
            return path + "?accessToken=" + accessToken;
        }
    }

    @Override
    public Resource loadMediaFile(Long mediaId, String accessToken) {
        validateAccessToken(mediaId, accessToken, MEDIA_FILE_TOKEN_PREFIX);
        return loadMediaFileInternal(mediaId, true);
    }

    @Override
    public Resource loadMediaFileAdmin(Long mediaId) {
        return loadMediaFileInternal(mediaId, false);
    }

    @Override
    public Resource loadMediaFileAdmin(Long mediaId, String accessToken) {
        validateAccessToken(mediaId, accessToken, ADMIN_MEDIA_FILE_TOKEN_PREFIX);
        return loadMediaFileInternal(mediaId, false);
    }

    private Resource loadMediaFileInternal(Long mediaId, boolean approvedOnly) {
        MediaEntity media = mediaMapper.findById(mediaId);
        if (media == null) {
            throw new BizException(ResultCode.NOT_FOUND, "media not found: " + mediaId);
        }
        if (approvedOnly && !"approved".equals(media.getReviewStatus())) {
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
        String configuredRootValue = storageProperties == null ? null : storageProperties.localRootDir();
        Path configuredRoot = Path.of(defaultIfBlank(configuredRootValue, DEFAULT_STORAGE_ROOT));
        Path root = resolveStorageRoot(configuredRoot);
        Path resolved;
        if (value.startsWith("/uploads/")) {
            resolved = root.resolve(value.substring("/uploads/".length())).normalize();
            return requireInsideRoot(resolved, root);
        }
        Path path = Path.of(value);
        if (path.isAbsolute()) {
            resolved = path.normalize();
            return requireInsideRoot(resolved, root);
        }
        resolved = root.resolve(value).normalize();
        return requireInsideRoot(resolved, root);
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

    private Path requireInsideRoot(Path path, Path root) {
        if (!path.startsWith(root)) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "media file path escapes storage root");
        }
        return path;
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String issueAccessToken(Long mediaId, String tokenPrefix) {
        if (mediaId == null) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "mediaId is required");
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(
                tokenPrefix + token,
                String.valueOf(mediaId),
                Duration.ofSeconds(mediaFileTokenTtlSeconds()));
        return token;
    }

    private void validateAccessToken(Long mediaId, String accessToken, String tokenPrefix) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new BizException(ResultCode.UNAUTHORIZED, "missing media access token");
        }
        String expectedMediaId = redisTemplate.opsForValue().get(tokenPrefix + accessToken);
        if (!String.valueOf(mediaId).equals(expectedMediaId)) {
            throw new BizException(ResultCode.UNAUTHORIZED, "invalid or expired media access token");
        }
    }

    private long mediaFileTokenTtlSeconds() {
        Long configured = storageProperties == null ? null : storageProperties.mediaFileTokenTtlSeconds();
        return configured == null || configured <= 0 ? DEFAULT_MEDIA_FILE_TOKEN_TTL_SECONDS : configured;
    }
}
