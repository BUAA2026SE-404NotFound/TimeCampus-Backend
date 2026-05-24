package com.notfound.timecampusserver.service.impl;

import com.notfound.timecampuscommon.exception.BizException;
import com.notfound.timecampusserver.config.StorageProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void storeReturnsAbsolutePathUnderConfiguredRoot() {
        LocalStorageService service = new LocalStorageService(new StorageProperties(tempDir.toString(), 10L, 600L));
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "img".getBytes());

        String storedPath = service.store(file, 7L);

        Path path = Path.of(storedPath);
        assertThat(path).isAbsolute();
        assertThat(path.normalize()).startsWith(tempDir.toAbsolutePath().normalize());
        assertThat(Files.isRegularFile(path)).isTrue();
        assertThat(path.getFileName().toString()).startsWith("7-");
        assertThat(path.getFileName().toString()).endsWith(".jpg");
    }

    @Test
    void storeCreatesDateDirectoryWhenMissing() {
        Path root = tempDir.resolve("cos-root");
        LocalStorageService service = new LocalStorageService(new StorageProperties(root.toString(), 10L, 600L));
        MockMultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", "img".getBytes());

        String storedPath = service.store(file, 1L);

        assertThat(Path.of(storedPath)).startsWith(root.toAbsolutePath().normalize());
        assertThat(Files.exists(Path.of(storedPath).getParent())).isTrue();
    }

    @Test
    void storeInfersExtensionFromContentTypeWhenFilenameHasNoExtension() {
        LocalStorageService service = new LocalStorageService(new StorageProperties(tempDir.toString(), 10L, 600L));
        MockMultipartFile file = new MockMultipartFile("file", "photo", "image/png", "img".getBytes());

        String storedPath = service.store(file, 1L);

        assertThat(storedPath).endsWith(".png");
        assertThat(Files.isRegularFile(Path.of(storedPath))).isTrue();
    }

    @Test
    void storeRejectsUnsupportedExtension() {
        LocalStorageService service = new LocalStorageService(new StorageProperties(tempDir.toString(), 10L, 600L));
        MockMultipartFile file = new MockMultipartFile("file", "shell.php", "application/octet-stream", "x".getBytes());

        assertThatThrownBy(() -> service.store(file, 1L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("only jpg/png images are allowed");
    }

    @Test
    void storeRejectsOversizedFile() {
        LocalStorageService service = new LocalStorageService(new StorageProperties(tempDir.toString(), 0L, 600L));
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "x".getBytes());

        assertThatThrownBy(() -> service.store(file, 1L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("image size must be <=");
    }
}
