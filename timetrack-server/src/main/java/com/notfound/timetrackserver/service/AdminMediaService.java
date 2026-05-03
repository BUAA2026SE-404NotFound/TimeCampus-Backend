package com.notfound.timetrackserver.service;

import com.notfound.timetrackpojo.dto.OfficialMediaImportRequest;
import com.notfound.timetrackpojo.vo.ImportResultVO;
import com.notfound.timetrackpojo.vo.MediaVO;

import java.util.List;

public interface AdminMediaService {

    ImportResultVO importOfficial(OfficialMediaImportRequest request);

    MediaVO getById(Long id);

    List<MediaVO> list(Long poiId, String type, String reviewStatus, Integer yearFrom, Integer yearTo);

    void deleteById(Long id);

    void approveMedia(Long id, Long reviewerId);

    void rejectMedia(Long id, Long reviewerId, String rejectReason);
}

