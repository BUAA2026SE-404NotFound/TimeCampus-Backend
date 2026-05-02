package com.notfound.timetrackserver.service.impl;

import com.notfound.timetrackpojo.entity.PoiEntity;
import com.notfound.timetrackpojo.vo.PoiVO;
import org.springframework.stereotype.Component;

@Component
public class PoiStructMapper {

    public PoiVO toVO(PoiEntity entity) {
        if (entity == null) {
            return null;
        }
        PoiVO vo = new PoiVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setLatitude(entity.getLatitude());
        vo.setLongitude(entity.getLongitude());
        vo.setDescription(entity.getDescription());
        vo.setFunFact(entity.getFunFact());
        vo.setStatus(entity.getStatus());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }
}

