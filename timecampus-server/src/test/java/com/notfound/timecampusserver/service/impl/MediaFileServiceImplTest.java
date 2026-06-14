package com.notfound.timecampusserver.service.impl;

import com.notfound.timecampuscommon.exception.BizException;
import com.notfound.timecampuspojo.entity.MediaEntity;
import com.notfound.timecampusserver.config.StorageProperties;
import com.notfound.timecampusserver.mapper.MediaMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MediaFileServiceImplTest {

    @TempDir
    Path tempDir;

    @Test
    void loadMediaFileServesUploadsUnderStorageRoot() throws Exception {
        Path root = Files.createDirectories(tempDir.resolve("storage/uploads"));
        Path mediaFile = Files.writeString(root.resolve("photo.svg"), "<svg/>");
        MediaFileServiceImpl service = serviceWithMedia("/uploads/photo.svg", root, "approved");

        var resource = service.loadMediaFile(9L, "valid-token");

        assertThat(resource.getFile().toPath()).isEqualTo(mediaFile);
    }

    @Test
    void loadMediaFileServesAbsolutePathUnderStorageRoot() throws Exception {
        Path root = Files.createDirectories(tempDir.resolve("cos"));
        Path mediaFile = Files.writeString(root.resolve("photo.jpg"), "img");
        MediaFileServiceImpl service = serviceWithMedia(mediaFile.toString(), root, "approved");

        var resource = service.loadMediaFile(9L, "valid-token");

        assertThat(resource.getFile().toPath()).isEqualTo(mediaFile);
    }

    @Test
    void loadMediaFileRejectsRemoteUrlBecauseFrontendShouldOpenItDirectly() throws Exception {
        Path root = Files.createDirectories(tempDir.resolve("cos"));
        MediaFileServiceImpl service = serviceWithMedia("https://example.com/photo.jpg", root, "approved");

        assertThatThrownBy(() -> service.loadMediaFile(9L, "valid-token"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("remote media should be accessed by url");
    }

    @Test
    void loadMediaFileRejectsNonApprovedContent() throws Exception {
        Path root = Files.createDirectories(tempDir.resolve("storage/uploads"));
        Files.writeString(root.resolve("photo.svg"), "<svg/>");
        MediaFileServiceImpl service = serviceWithMedia("/uploads/photo.svg", root, "pending");

        assertThatThrownBy(() -> service.loadMediaFile(9L, "valid-token"))
                .isInstanceOf(BizException.class);
    }

    @Test
    void loadMediaFileAdminServesNonApprovedContent() throws Exception {
        Path root = Files.createDirectories(tempDir.resolve("storage/uploads"));
        Path mediaFile = Files.writeString(root.resolve("photo.svg"), "<svg/>");
        MediaFileServiceImpl service = serviceWithMedia("/uploads/photo.svg", root, "pending");

        var resource = service.loadMediaFileAdmin(9L);

        assertThat(resource.getFile().toPath()).isEqualTo(mediaFile);
    }

    @Test
    void loadMediaFileAdminWithAccessTokenServesNonApprovedContent() throws Exception {
        Path root = Files.createDirectories(tempDir.resolve("storage/uploads"));
        Path mediaFile = Files.writeString(root.resolve("photo.svg"), "<svg/>");
        MediaFileServiceImpl service = serviceWithMedia("/uploads/photo.svg", root, "pending");

        var resource = service.loadMediaFileAdmin(9L, "valid-admin-token");

        assertThat(resource.getFile().toPath()).isEqualTo(mediaFile);
    }

    @Test
    void loadMediaFileAdminWithAccessTokenRejectsUserMediaToken() throws Exception {
        Path root = Files.createDirectories(tempDir.resolve("storage/uploads"));
        Files.writeString(root.resolve("photo.svg"), "<svg/>");
        MediaFileServiceImpl service = serviceWithMedia("/uploads/photo.svg", root, "pending");

        assertThatThrownBy(() -> service.loadMediaFileAdmin(9L, "valid-token"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("invalid or expired media access token");
    }

    @Test
    void loadMediaFileRejectsTraversalOutsideStorageRoot() throws Exception {
        Path root = Files.createDirectories(tempDir.resolve("storage/uploads"));
        Files.writeString(tempDir.resolve("application-dev.yaml"), "secret");
        MediaFileServiceImpl service = serviceWithMedia("/uploads/../../application-dev.yaml", root, "approved");

        assertThatThrownBy(() -> service.loadMediaFile(9L, "valid-token"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("escapes storage root");
    }

    @Test
    void loadMediaFileRejectsAbsolutePathOutsideStorageRoot() throws Exception {
        Path root = Files.createDirectories(tempDir.resolve("storage/uploads"));
        String outsidePath = tempDir.resolve("secret.txt").toAbsolutePath().toString();
        Files.writeString(Path.of(outsidePath), "secret");
        MediaFileServiceImpl service = serviceWithMedia(outsidePath, root, "approved");

        assertThatThrownBy(() -> service.loadMediaFile(9L, "valid-token"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("escapes storage root");
    }

    @Test
    void previewUrlIssuesSignedAccessTokenForLocalFile() throws Exception {
        Path root = Files.createDirectories(tempDir.resolve("storage/uploads"));
        MediaFileServiceImpl service = serviceWithMedia("/uploads/photo.jpg", root, "approved");

        String url = service.previewUrl(9L, "/uploads/photo.jpg");

        assertThat(url).startsWith("/api/v1/media/9/file?accessToken=");
    }

    @Test
    void adminPreviewUrlIssuesSignedAccessTokenForLocalFile() throws Exception {
        Path root = Files.createDirectories(tempDir.resolve("storage/uploads"));
        MediaFileServiceImpl service = serviceWithMedia("/uploads/photo.jpg", root, "pending");

        String url = service.adminPreviewUrl(9L, "/uploads/photo.jpg");

        assertThat(url).startsWith("/api/v1/admin/media/9/file?accessToken=");
    }

    @Test
    void loadMediaFileRejectsMissingToken() throws Exception {
        Path root = Files.createDirectories(tempDir.resolve("storage/uploads"));
        MediaFileServiceImpl service = serviceWithMedia("/uploads/photo.jpg", root, "approved");

        assertThatThrownBy(() -> service.loadMediaFile(9L, null))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("missing media access token");
    }

    private MediaFileServiceImpl serviceWithMedia(String imagePath, Path root, String reviewStatus) {
        MediaMapper mediaMapper = mock(MediaMapper.class);
        MediaEntity media = new MediaEntity();
        media.setId(9L);
        media.setImagePath(imagePath);
        media.setReviewStatus(reviewStatus);
        when(mediaMapper.findById(9L)).thenReturn(media);
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("media:file:token:valid-token")).thenReturn("9");
        when(valueOperations.get("admin:media:file:token:valid-admin-token")).thenReturn("9");
        org.mockito.Mockito.doNothing().when(valueOperations)
                .set(any(String.class), eq("9"), any(Duration.class));
        return new MediaFileServiceImpl(mediaMapper, new StorageProperties(root.toString(), 10L, 600L), redisTemplate);
    }
}
