package com.notfound.timetrackserver.service;

import com.notfound.timetrackpojo.entity.LogEntity;

import java.util.List;

public interface LogService {

    void record(String operatorType, Long operatorId, String type, String action,
                String targetType, Long targetId, String detail);

    List<LogEntity> list(String operatorType, String type, String action, String targetType, Integer limit);
}
