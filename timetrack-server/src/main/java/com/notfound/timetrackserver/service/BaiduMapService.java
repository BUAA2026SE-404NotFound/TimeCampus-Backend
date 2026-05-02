package com.notfound.timetrackserver.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface BaiduMapService {

    /**
     * 地理编码：地址 -> 经纬度
     * @param address 详细地址，如"北京市海淀区学院路37号"
     * @return 纬度、经度，格式 [lat, lng]
     */
    BigDecimal[] geocoding(String address);

    /**
     * 逆地理编码：经纬度 -> 地址描述
     * @param lat 纬度
     * @param lng 经度
     * @return 结构化地址
     */
    String reverseGeocoding(BigDecimal lat, BigDecimal lng);

    /**
     * 周边检索：根据关键词和中心点半径检索POI
     * @param query 关键词
     * @param lat 中心点纬度
     * @param lng 中心点经度
     * @param radius 半径（米）
     * @return POI列表，每个元素包含name, address, lat, lng等
     */
    List<Map<String, Object>> placeSearch(String query, BigDecimal lat, BigDecimal lng, int radius);
}
