package com.notfound.timecampusserver.service;

import com.notfound.timecampuspojo.dto.PoiCreateRequest;
import com.notfound.timecampuspojo.dto.PoiUpdateRequest;
import com.notfound.timecampuspojo.vo.PoiVO;

import java.util.List;

public interface PoiService {

    PoiVO create(PoiCreateRequest request);

    PoiVO update(Long id, PoiUpdateRequest request);

    void delete(Long id);

    PoiVO getById(Long id);

    List<PoiVO> list(Integer status, String keyword);
}

