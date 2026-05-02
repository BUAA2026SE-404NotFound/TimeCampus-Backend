package com.notfound.timetrackserver.service.impl;

import com.notfound.timetrackserver.service.BaiduMapService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BaiduMapServiceImplTest {

    @Autowired
    private BaiduMapService baiduMapService;

    @Test
    void testGeocoding() {
        BigDecimal[] result = baiduMapService.geocoding("北京市海淀区学院路37号");
        assertNotNull(result);
        assertEquals(2, result.length);
        System.out.println("lat=" + result[0] + ", lng=" + result[1]);
    }

    @Test
    void testReverseGeocoding() {
        BigDecimal[] latLng = baiduMapService.geocoding("北京市海淀区学院路37号");
        String address = baiduMapService.reverseGeocoding(latLng[0], latLng[1]);
        assertNotNull(address);
        System.out.println("address = " + address);
    }

    @Test
    void testPlaceSearch() {
        List<Map<String, Object>> pois = baiduMapService.placeSearch("餐厅", new BigDecimal("39.9042"), new BigDecimal("116.4074"), 500);
        assertNotNull(pois);
        assertTrue(pois.size() > 0);
    }
}
