package com.notfound.timetrackserver.service.impl;

import com.notfound.timetrackcommon.api.ResultCode;
import com.notfound.timetrackcommon.exception.BizException;
import com.notfound.timetrackpojo.dto.PoiCreateRequest;
import com.notfound.timetrackpojo.dto.PoiUpdateRequest;
import com.notfound.timetrackpojo.entity.PoiEntity;
import com.notfound.timetrackpojo.vo.PoiVO;
import com.notfound.timetrackserver.mapper.PoiMapper;
import com.notfound.timetrackserver.security.AdminContext;
import com.notfound.timetrackserver.service.LogService;
import com.notfound.timetrackserver.service.PoiService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PoiServiceImpl implements PoiService {

    private final PoiMapper poiMapper;
    private final PoiStructMapper poiStructMapper;
    private final LogService logService;

    public PoiServiceImpl(PoiMapper poiMapper, PoiStructMapper poiStructMapper, LogService logService) {
        this.poiMapper = poiMapper;
        this.poiStructMapper = poiStructMapper;
        this.logService = logService;
    }

    @Override
    public PoiVO create(PoiCreateRequest request) {
        PoiEntity entity = new PoiEntity();
        entity.setName(request.getName());
        entity.setLatitude(request.getLatitude());
        entity.setLongitude(request.getLongitude());
        entity.setDescription(request.getDescription());
        entity.setFunFact(request.getFunFact());
        entity.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        poiMapper.insert(entity);
        logService.record("ADMIN", AdminContext.getAdminId(), "content", "create_poi", "poi", entity.getId(), entity.getName());
        return poiStructMapper.toVO(entity);
    }

    @Override
    public PoiVO update(Long id, PoiUpdateRequest request) {
        PoiEntity existing = poiMapper.findById(id);
        if (existing == null) {
            throw new BizException(ResultCode.NOT_FOUND, "poi not found: " + id);
        }
        existing.setName(request.getName());
        existing.setLatitude(request.getLatitude());
        existing.setLongitude(request.getLongitude());
        existing.setDescription(request.getDescription());
        existing.setFunFact(request.getFunFact());
        existing.setStatus(request.getStatus() == null ? existing.getStatus() : request.getStatus());
        existing.setUpdateTime(LocalDateTime.now());
        poiMapper.updateById(existing);
        logService.record("ADMIN", AdminContext.getAdminId(), "content", "update_poi", "poi", existing.getId(), existing.getName());
        return poiStructMapper.toVO(existing);
    }

    @Override
    public void delete(Long id) {
        poiMapper.deleteById(id);
        logService.record("ADMIN", AdminContext.getAdminId(), "content", "delete_poi", "poi", id, null);
    }

    @Override
    public PoiVO getById(Long id) {
        PoiEntity entity = poiMapper.findById(id);
        if (entity == null) {
            throw new BizException(ResultCode.NOT_FOUND, "poi not found: " + id);
        }
        return poiStructMapper.toVO(entity);
    }

    @Override
    public List<PoiVO> list(Integer status, String keyword) {
        return poiMapper.list(status, keyword)
                .stream()
                .map(poiStructMapper::toVO)
                .collect(Collectors.toList());
    }
}
