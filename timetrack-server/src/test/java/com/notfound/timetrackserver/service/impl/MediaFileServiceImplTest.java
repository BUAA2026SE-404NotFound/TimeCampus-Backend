package com.notfound.timetrackserver.service.impl;

import com.notfound.timetrackcommon.exception.BizException;
import com.notfound.timetrackpojo.entity.MediaEntity;
import com.notfound.timetrackserver.config.StorageProperties;
import com.notfound.timetrackserver.mapper.MediaMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

        var resource = service.loadMediaFile(9L);

        assertThat(resource.getFile().toPath()).isEqualTo(mediaFile);
    }

    @Test
    void loadMediaFileRejectsNonApprovedContent() throws Exception {
        Path root = Files.createDirectories(tempDir.resolve("storage/uploads"));
        Files.writeString(root.resolve("photo.svg"), "<svg/>");
        MediaFileServiceImpl service = serviceWithMedia("/uploads/photo.svg", root, "pending");

        assertThatThrownBy(() -> service.loadMediaFile(9L))
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
    void loadMediaFileRejectsTraversalOutsideStorageRoot() throws Exception {
        Path root = Files.createDirectories(tempDir.resolve("storage/uploads"));
        Files.writeString(tempDir.resolve("application-dev.yaml"), "secret");
        MediaFileServiceImpl service = serviceWithMedia("/uploads/../../application-dev.yaml", root, "approved");

        assertThatThrownBy(() -> service.loadMediaFile(9L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("escapes storage root");
    }

    @Test
    void loadMediaFileRejectsAbsolutePathOutsideStorageRoot() throws Exception {
        Path root = Files.createDirectories(tempDir.resolve("storage/uploads"));
        String outsidePath = tempDir.resolve("secret.txt").toAbsolutePath().toString();
        Files.writeString(Path.of(outsidePath), "secret");
        MediaFileServiceImpl service = serviceWithMedia(outsidePath, root, "approved");

        assertThatThrownBy(() -> service.loadMediaFile(9L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("escapes storage root");
    }

    private MediaFileServiceImpl serviceWithMedia(String imagePath, Path root, String reviewStatus) {
        MediaMapper mediaMapper = mock(MediaMapper.class);
        MediaEntity media = new MediaEntity();
        media.setId(9L);
        media.setImagePath(imagePath);
        media.setReviewStatus(reviewStatus);
        when(mediaMapper.findById(9L)).thenReturn(media);
        return new MediaFileServiceImpl(mediaMapper, new StorageProperties(root.toString(), 10L));
    }
}
