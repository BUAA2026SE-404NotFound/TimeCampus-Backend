package com.notfound.timecampusserver.aop;

import com.notfound.timecampuspojo.entity.PoiEntity;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TimeFillAspectTest {

    private final TimeFillAspect aspect = new TimeFillAspect();

    @Test
    void insertFillsCreateAndUpdateTime() {
        PoiEntity entity = new PoiEntity();
        JoinPoint joinPoint = mockJoinPoint(entity);

        aspect.fillCreateAndUpdateTime(joinPoint);

        assertThat(entity.getCreateTime()).isNotNull();
        assertThat(entity.getUpdateTime()).isNotNull();
    }

    @Test
    void updateRefreshesUpdateTimeWithoutChangingCreateTime() {
        LocalDateTime created = LocalDateTime.of(2026, 1, 1, 0, 0);
        PoiEntity entity = new PoiEntity();
        entity.setCreateTime(created);
        entity.setUpdateTime(LocalDateTime.of(2026, 1, 1, 0, 0));
        JoinPoint joinPoint = mockJoinPoint(entity);

        aspect.fillUpdateTime(joinPoint);

        assertThat(entity.getCreateTime()).isEqualTo(created);
        assertThat(entity.getUpdateTime()).isAfter(created);
    }

    private JoinPoint mockJoinPoint(Object arg) {
        JoinPoint joinPoint = mock(JoinPoint.class);
        when(joinPoint.getArgs()).thenReturn(new Object[]{arg});
        return joinPoint;
    }
}
