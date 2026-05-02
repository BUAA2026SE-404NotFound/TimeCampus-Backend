package com.notfound.timetrackserver.service;

import org.springframework.core.io.Resource;

public interface MediaFileService {
    String previewUrl(Long mediaId, String imagePath);

    Resource loadMediaFile(Long mediaId);

    String contentType(Resource resource);
}
