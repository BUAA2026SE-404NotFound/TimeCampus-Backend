package com.notfound.timecampusserver.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SeedreamImageService {

    List<SeedreamBackground> listBackgrounds();

    SeedreamBackground getBackground(String backgroundId);

    Resource loadBackgroundResource(String backgroundId);

    SeedreamGenerationResult generate(String backgroundId, MultipartFile personImage);

    record SeedreamBackground(
            String id,
            String title,
            String year,
            String description,
            String previewUrl
    ) {
    }

    record SeedreamGenerationResult(
            String imageUrl,
            SeedreamBackground background,
            String model,
            String promptVersion
    ) {
    }
}
