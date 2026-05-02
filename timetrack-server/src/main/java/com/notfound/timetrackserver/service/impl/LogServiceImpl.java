package com.notfound.timetrackserver.service.impl;

import com.notfound.timetrackpojo.entity.LogEntity;
import com.notfound.timetrackserver.mapper.LogMapper;
import com.notfound.timetrackserver.service.LogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LogServiceImpl implements LogService {

    private final LogMapper logMapper;

    public LogServiceImpl(LogMapper logMapper) {
        this.logMapper = logMapper;
    }

    @Override
    public void record(String operatorType, Long operatorId, String type, String action,
                       String targetType, Long targetId, String detail) {
        LogEntity entity = new LogEntity();
        entity.setOperatorType(operatorType);
        entity.setOperatorId(operatorId);
        entity.setType(type);
        entity.setAction(action);
        entity.setTargetType(targetType);
        entity.setTargetId(targetId);
        entity.setDetail(detail);
        entity.setCreateTime(LocalDateTime.now());
        logMapper.insert(entity);
    }

    @Override
    public List<LogEntity> list(String operatorType, String type, String action, String targetType, Integer limit) {
        int safeLimit = limit == null ? 100 : Math.max(1, Math.min(limit, 500));
        return logMapper.list(blankToNull(operatorType), blankToNull(type), blankToNull(action), blankToNull(targetType), safeLimit);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
