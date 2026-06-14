package com.notfound.timecampusserver.service;

import com.notfound.timecampuspojo.vo.TimelineItemVO;

import java.util.List;

public interface TimelineService {

    List<TimelineItemVO> list(Long userId,
                              int startYear,
                              int endYear,
                              String type,
                              Long poiId);
}
