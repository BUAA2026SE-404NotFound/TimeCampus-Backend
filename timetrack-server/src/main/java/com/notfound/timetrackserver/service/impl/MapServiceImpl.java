package com.notfound.timetrackserver.service.impl;

import com.notfound.timetrackpojo.entity.MediaEntity;
import com.notfound.timetrackpojo.entity.PoiEntity;
import com.notfound.timetrackpojo.vo.MapHomeVO;
import com.notfound.timetrackpojo.vo.MapMediaVO;
import com.notfound.timetrackpojo.vo.MapPoiVO;
import com.notfound.timetrackserver.mapper.MediaMapper;
import com.notfound.timetrackserver.mapper.PoiMapper;
import com.notfound.timetrackserver.service.MapService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MapServiceImpl implements MapService {

    private static final String TYPE_OFFICIAL = "official";
    private static final String REVIEW_APPROVED = "approved";

    private final PoiMapper poiMapper;
    private final MediaMapper mediaMapper;

    public MapServiceImpl(PoiMapper poiMapper, MediaMapper mediaMapper) {
        this.poiMapper = poiMapper;
        this.mediaMapper = mediaMapper;
    }

    @Override
    public MapHomeVO getHome(Integer year) {
        List<PoiEntity> pois = poiMapper.list(1, null);
        MapHomeVO homeVO = new MapHomeVO();
        if (pois.isEmpty()) {
            homeVO.setPois(List.of());
            return homeVO;
        }

        List<Long> poiIds = pois.stream().map(PoiEntity::getId).collect(Collectors.toList());
        List<MediaEntity> mediaList = mediaMapper.listByPoiIds(poiIds, TYPE_OFFICIAL, REVIEW_APPROVED);

        Map<Long, List<MediaEntity>> mediaByPoi = new HashMap<>();
        for (MediaEntity media : mediaList) {
            mediaByPoi.computeIfAbsent(media.getPoiId(), k -> new ArrayList<>()).add(media);
        }

        List<MapPoiVO> mapPois = new ArrayList<>();
        for (PoiEntity poi : pois) {
            MapPoiVO vo = new MapPoiVO();
            vo.setId(poi.getId());
            vo.setName(poi.getName());
            vo.setLatitude(poi.getLatitude());
            vo.setLongitude(poi.getLongitude());
            vo.setStatus(poi.getStatus());
            vo.setDescription(poi.getDescription());

            List<MediaEntity> list = mediaByPoi.getOrDefault(poi.getId(), List.of());
            vo.setAvailableYears(list.stream().map(MediaEntity::getYear).distinct().sorted().collect(Collectors.toList()));
            vo.setCoverImagePath(selectCover(list, year));
            vo.setMediaList(list.stream().map(this::toMapMediaVO).collect(Collectors.toList()));

            mapPois.add(vo);
        }

        homeVO.setPois(mapPois);
        return homeVO;
    }

    private String selectCover(List<MediaEntity> list, Integer year) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        if (year == null) {
            return list.get(0).getImagePath();
        }
        return list.stream()
                .min(Comparator.comparingInt(m -> Math.abs(m.getYear() - year)))
                .map(MediaEntity::getImagePath)
                .orElse(list.get(0).getImagePath());
    }

    private MapMediaVO toMapMediaVO(MediaEntity entity) {
        MapMediaVO vo = new MapMediaVO();
        vo.setId(entity.getId());
        vo.setYear(entity.getYear());
        vo.setImagePath(entity.getImagePath());
        vo.setDescription(entity.getDescription());
        vo.setType(entity.getType());
        return vo;
    }
}

