package com.notfound.timetrackserver.service;

import com.notfound.timetrackpojo.vo.MapHomeVO;
import com.notfound.timetrackpojo.vo.MapMediaVO;

public interface MapService {

    MapHomeVO getHome(Integer year);

    MapMediaVO getTimeMachineMedia(Long poiId, Integer year);
}

