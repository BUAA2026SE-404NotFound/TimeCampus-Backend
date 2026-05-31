package com.notfound.timecampusserver.service;

import org.springframework.core.io.Resource;

public interface MediaFileService {
    String previewUrl(Long mediaId, String imagePath);

    String adminPreviewUrl(Long mediaId, String imagePath);

    /** Load media file for the user endpoint; only signed, approved media is served. */
    Resource loadMediaFile(Long mediaId, String accessToken);

    /** Load media file for the admin endpoint; no review-status restriction. */
    Resource loadMediaFileAdmin(Long mediaId);

    /** Load media file for the signed admin endpoint; no review-status restriction. */
    Resource loadMediaFileAdmin(Long mediaId, String accessToken);

    String contentType(Resource resource);
}
