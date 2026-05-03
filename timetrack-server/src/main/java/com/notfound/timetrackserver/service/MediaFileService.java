package com.notfound.timetrackserver.service;

import org.springframework.core.io.Resource;

public interface MediaFileService {
    String previewUrl(Long mediaId, String imagePath);

    /** Load media file for the public endpoint; only {@code approved} media is served. */
    Resource loadMediaFile(Long mediaId);

    /** Load media file for the admin endpoint; no review-status restriction. */
    Resource loadMediaFileAdmin(Long mediaId);

    String contentType(Resource resource);
}
