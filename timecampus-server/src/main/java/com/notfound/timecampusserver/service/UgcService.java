package com.notfound.timecampusserver.service;

import com.notfound.timecampuspojo.vo.MediaVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UgcService {

    MediaVO upload(MultipartFile file, Long poiId, Integer year, String description, String source, Long userId);

    List<MediaVO> list(String status);

    MediaVO approve(Long id, Long reviewerId);

    MediaVO reject(Long id, String reason, Long reviewerId);
}
