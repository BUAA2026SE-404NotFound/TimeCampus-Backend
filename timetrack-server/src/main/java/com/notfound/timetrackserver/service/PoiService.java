package com.notfound.timetrackserver.service;

import com.notfound.timetrackpojo.dto.PoiCreateRequest;
import com.notfound.timetrackpojo.dto.PoiUpdateRequest;
import com.notfound.timetrackpojo.vo.PoiVO;

import java.util.List;

public interface PoiService {

    PoiVO create(PoiCreateRequest request);

    PoiVO update(Long id, PoiUpdateRequest request);

    void delete(Long id);

    PoiVO getById(Long id);

    List<PoiVO> list(Integer status, String keyword);
}

