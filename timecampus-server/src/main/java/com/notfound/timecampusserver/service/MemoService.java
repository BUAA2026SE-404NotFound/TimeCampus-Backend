package com.notfound.timecampusserver.service;

import com.notfound.timecampuspojo.vo.MediaVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MemoService {
    MediaVO createMemo(Long userId, Long poiId, Integer year, String description, MultipartFile file);
    List<MediaVO> listMemos(Long userId, Long poiId, Integer yearFrom, Integer yearTo);
    void deleteMemo(Long id, Long userId);
}
