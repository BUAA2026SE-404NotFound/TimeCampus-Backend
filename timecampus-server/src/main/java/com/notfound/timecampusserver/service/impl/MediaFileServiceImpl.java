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

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Iterator;
import java.util.UUID;

@Service
public class MediaFileServiceImpl implements MediaFileService {

    private static final String DEFAULT_STORAGE_ROOT = LocalStorageService.DEFAULT_STORAGE_ROOT;
    private static final String MEDIA_FILE_TOKEN_PREFIX = "media:file:token:";
    private static final String ADMIN_MEDIA_FILE_TOKEN_PREFIX = "admin:media:file:token:";
    private static final long DEFAULT_MEDIA_FILE_TOKEN_TTL_SECONDS = 600L;
    private static final int MIN_THUMBNAIL_SIZE = 32;
    private static final int MAX_THUMBNAIL_SIZE = 2048;
    private static final float THUMBNAIL_JPEG_QUALITY = 0.82f;

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
        return previewUrl(mediaId, imagePath, null);
    }

    @Override
    public String previewUrl(Long mediaId, String imagePath, Integer size) {
        return signedPreviewUrl(mediaId, imagePath, "/api/v1/media/" + mediaId + "/file", MEDIA_FILE_TOKEN_PREFIX, size);
    }

    @Override
    public String adminPreviewUrl(Long mediaId, String imagePath) {
        return adminPreviewUrl(mediaId, imagePath, null);
    }

    @Override
    public String adminPreviewUrl(Long mediaId, String imagePath, Integer size) {
        return signedPreviewUrl(mediaId, imagePath, "/api/v1/admin/media/" + mediaId + "/file", ADMIN_MEDIA_FILE_TOKEN_PREFIX, size);
    }

    private String signedPreviewUrl(Long mediaId, String imagePath, String path, String tokenPrefix, Integer size) {
        if (imagePath == null || imagePath.isBlank()) {
            return null;
        }
        String value = imagePath.trim();
        if (value.startsWith("http://") || value.startsWith("https://")) {
            return value;
        }
        Integer normalizedSize = normalizeSize(size);
        String accessToken = issueAccessToken(mediaId, tokenPrefix);
        try {
            var builder = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path(path)
                    .queryParam("accessToken", accessToken);
            if (normalizedSize != null) {
                builder.queryParam("size", normalizedSize);
            }
            return builder.toUriString();
        } catch (IllegalStateException e) {
            String url = path + "?accessToken=" + accessToken;
            return normalizedSize == null ? url : url + "&size=" + normalizedSize;
        }
    }

    @Override
    public Resource loadMediaFile(Long mediaId, String accessToken) {
        return loadMediaFile(mediaId, accessToken, null);
    }

    @Override
    public Resource loadMediaFile(Long mediaId, String accessToken, Integer size) {
        validateAccessToken(mediaId, accessToken, MEDIA_FILE_TOKEN_PREFIX);
        return loadMediaFileInternal(mediaId, true, size);
    }

    @Override
    public Resource loadMediaFileAdmin(Long mediaId) {
        return loadMediaFileAdmin(mediaId, (Integer) null);
    }

    @Override
    public Resource loadMediaFileAdmin(Long mediaId, Integer size) {
        return loadMediaFileInternal(mediaId, false, size);
    }

    @Override
    public Resource loadMediaFileAdmin(Long mediaId, String accessToken) {
        return loadMediaFileAdmin(mediaId, accessToken, null);
    }

    @Override
    public Resource loadMediaFileAdmin(Long mediaId, String accessToken, Integer size) {
        validateAccessToken(mediaId, accessToken, ADMIN_MEDIA_FILE_TOKEN_PREFIX);
        return loadMediaFileInternal(mediaId, false, size);
    }

    private Resource loadMediaFileInternal(Long mediaId, boolean approvedOnly, Integer size) {
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
        Integer normalizedSize = normalizeSize(size);
        if (normalizedSize != null) {
            path = thumbnailPath(mediaId, path, normalizedSize);
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
        String latestKey = tokenPrefix + "latest:" + mediaId;
        String latestToken = redisTemplate.opsForValue().get(latestKey);
        if (latestToken != null && String.valueOf(mediaId).equals(redisTemplate.opsForValue().get(tokenPrefix + latestToken))) {
            return latestToken;
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        Duration ttl = Duration.ofSeconds(mediaFileTokenTtlSeconds());
        redisTemplate.opsForValue().set(
                tokenPrefix + token,
                String.valueOf(mediaId),
                ttl);
        redisTemplate.opsForValue().set(latestKey, token, ttl);
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

    private Integer normalizeSize(Integer size) {
        if (size == null) {
            return null;
        }
        if (size < MIN_THUMBNAIL_SIZE || size > MAX_THUMBNAIL_SIZE) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "size must be between " + MIN_THUMBNAIL_SIZE + " and " + MAX_THUMBNAIL_SIZE);
        }
        return size;
    }

    private Path thumbnailPath(Long mediaId, Path original, int size) {
        try {
            BufferedImage image = ImageIO.read(original.toFile());
            if (image == null) {
                return original;
            }
            Path thumbnailDir = storageRoot().resolve(".thumbs").resolve(String.valueOf(size)).normalize();
            if (!thumbnailDir.startsWith(storageRoot())) {
                throw new BizException(ResultCode.INTERNAL_ERROR, "thumbnail path escapes storage root");
            }
            Files.createDirectories(thumbnailDir);
            long modified = Files.getLastModifiedTime(original).toMillis();
            Path thumbnail = thumbnailDir.resolve(mediaId + "-" + modified + ".jpg");
            if (Files.isRegularFile(thumbnail)) {
                return thumbnail;
            }

            BufferedImage scaled = scaleToFit(image, size);
            writeJpeg(scaled, thumbnail, THUMBNAIL_JPEG_QUALITY);
            return thumbnail;
        } catch (IOException e) {
            throw new BizException(ResultCode.INTERNAL_ERROR, "failed to create thumbnail: " + e.getMessage());
        }
    }

    private BufferedImage scaleToFit(BufferedImage source, int maxSize) {
        int width = source.getWidth();
        int height = source.getHeight();
        double ratio = Math.min(1D, (double) maxSize / Math.max(width, height));
        int targetWidth = Math.max(1, (int) Math.round(width * ratio));
        int targetHeight = Math.max(1, (int) Math.round(height * ratio));
        BufferedImage target = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = target.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setColor(java.awt.Color.WHITE);
            graphics.fillRect(0, 0, targetWidth, targetHeight);
            graphics.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        } finally {
            graphics.dispose();
        }
        return target;
    }

    private void writeJpeg(BufferedImage image, Path target, float quality) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            throw new IOException("no jpeg writer available");
        }
        ImageWriter writer = writers.next();
        try (ImageOutputStream output = ImageIO.createImageOutputStream(target.toFile())) {
            ImageWriteParam params = writer.getDefaultWriteParam();
            params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            params.setCompressionQuality(quality);
            writer.setOutput(output);
            writer.write(null, new IIOImage(image, null, null), params);
        } finally {
            writer.dispose();
        }
    }

    private Path storageRoot() {
        String configuredRootValue = storageProperties == null ? null : storageProperties.localRootDir();
        return resolveStorageRoot(Path.of(defaultIfBlank(configuredRootValue, DEFAULT_STORAGE_ROOT)));
    }
}
