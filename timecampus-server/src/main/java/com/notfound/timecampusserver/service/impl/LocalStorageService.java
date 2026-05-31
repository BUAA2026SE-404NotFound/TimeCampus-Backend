package com.notfound.timecampusserver.service.impl;

import com.notfound.timecampuscommon.api.ResultCode;
import com.notfound.timecampuscommon.exception.BizException;
import com.notfound.timecampusserver.config.StorageProperties;
import com.notfound.timecampusserver.service.StorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;

@Service
public class LocalStorageService implements StorageService {

    static final String DEFAULT_STORAGE_ROOT = "/home/ubuntu/cos";
    private static final long COMPRESSED_IMAGE_LIMIT_BYTES = 2L * 1024 * 1024;
    private static final float MIN_JPEG_QUALITY = 0.55f;
    private static final float MAX_JPEG_QUALITY = 0.9f;

    private final StorageProperties properties;

    public LocalStorageService(StorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public String store(MultipartFile file, Long userId) {
        validate(file);
        StoredImage storedImage = prepareImage(file);
        String ext = storedImage.extension();
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
            storedImage.writeTo(target);
        } catch (IOException e) {
            throw new BizException(ResultCode.INTERNAL_ERROR, "failed to store file: " + e.getMessage());
        }
        return target.toString();
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "file is required");
        }
        long maxMb = properties.maxFileSizeMb() == null ? 20 : properties.maxFileSizeMb();
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

    private StoredImage prepareImage(MultipartFile file) {
        try {
            byte[] original = file.getBytes();
            if (original.length <= COMPRESSED_IMAGE_LIMIT_BYTES) {
                return new StoredImage(extension(file), original);
            }
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                return new StoredImage(extension(file), original);
            }
            byte[] compressed = compressJpegUnderLimit(image);
            return new StoredImage("jpg", compressed);
        } catch (IOException e) {
            throw new BizException(ResultCode.INTERNAL_ERROR, "failed to process image: " + e.getMessage());
        }
    }

    private byte[] compressJpegUnderLimit(BufferedImage source) throws IOException {
        BufferedImage image = toRgb(source);
        float quality = MAX_JPEG_QUALITY;
        byte[] output = writeJpeg(image, quality);
        while (output.length > COMPRESSED_IMAGE_LIMIT_BYTES && quality > MIN_JPEG_QUALITY) {
            quality = Math.max(MIN_JPEG_QUALITY, quality - 0.08f);
            output = writeJpeg(image, quality);
        }
        while (output.length > COMPRESSED_IMAGE_LIMIT_BYTES && Math.max(image.getWidth(), image.getHeight()) > 320) {
            image = scale(image, 0.85D);
            output = writeJpeg(image, MIN_JPEG_QUALITY);
        }
        if (output.length > COMPRESSED_IMAGE_LIMIT_BYTES) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "image cannot be compressed under 2MB");
        }
        return output;
    }

    private BufferedImage toRgb(BufferedImage source) {
        BufferedImage target = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = target.createGraphics();
        try {
            graphics.setColor(java.awt.Color.WHITE);
            graphics.fillRect(0, 0, target.getWidth(), target.getHeight());
            graphics.drawImage(source, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        return target;
    }

    private BufferedImage scale(BufferedImage source, double ratio) {
        int width = Math.max(1, (int) Math.round(source.getWidth() * ratio));
        int height = Math.max(1, (int) Math.round(source.getHeight() * ratio));
        BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = target.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.drawImage(source, 0, 0, width, height, null);
        } finally {
            graphics.dispose();
        }
        return target;
    }

    private byte[] writeJpeg(BufferedImage image, float quality) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            throw new IOException("no jpeg writer available");
        }
        ImageWriter writer = writers.next();
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             ImageOutputStream output = ImageIO.createImageOutputStream(bytes)) {
            ImageWriteParam params = writer.getDefaultWriteParam();
            params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            params.setCompressionQuality(quality);
            writer.setOutput(output);
            writer.write(null, new IIOImage(image, null, null), params);
            return bytes.toByteArray();
        } finally {
            writer.dispose();
        }
    }

    private record StoredImage(String extension, byte[] bytes) {
        void writeTo(Path target) throws IOException {
            try (OutputStream output = Files.newOutputStream(target)) {
                output.write(bytes);
            }
        }
    }
}
