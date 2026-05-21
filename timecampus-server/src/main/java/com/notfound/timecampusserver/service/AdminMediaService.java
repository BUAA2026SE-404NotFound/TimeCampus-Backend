package com.notfound.timecampusserver.service;

import com.notfound.timecampuspojo.dto.OfficialMediaImportRequest;
import com.notfound.timecampuspojo.vo.ImportResultVO;
import com.notfound.timecampuspojo.vo.MediaVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AdminMediaService {

    ImportResultVO importOfficial(OfficialMediaImportRequest request);

    MediaVO uploadOfficial(MultipartFile file, Long poiId, Integer year, String description, Long reviewerId);

    MediaVO getById(Long id);

    List<MediaVO> list(Long poiId, String type, String reviewStatus, Integer yearFrom, Integer yearTo);

    void deleteById(Long id);

    void approveMedia(Long id, Long reviewerId);

    void rejectMedia(Long id, Long reviewerId, String rejectReason);
}
