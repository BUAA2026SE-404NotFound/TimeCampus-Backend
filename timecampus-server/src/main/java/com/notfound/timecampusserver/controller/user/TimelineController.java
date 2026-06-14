package com.notfound.timecampusserver.controller.user;

import com.notfound.timecampuscommon.api.ApiResponse;
import com.notfound.timecampuscommon.api.ResultCode;
import com.notfound.timecampuscommon.exception.BizException;
import com.notfound.timecampuspojo.vo.TimelineItemVO;
import com.notfound.timecampusserver.security.UserAuthInterceptor;
import com.notfound.timecampusserver.service.TimelineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Timeline", description = "时间线聚合查询")
@RestController
@RequestMapping("/api/v1")
public class TimelineController {

    private final TimelineService timelineService;
    private final UserAuthInterceptor userAuthInterceptor;

    public TimelineController(TimelineService timelineService, UserAuthInterceptor userAuthInterceptor) {
        this.timelineService = timelineService;
        this.userAuthInterceptor = userAuthInterceptor;
    }

    @GetMapping("/timeline")
    @Operation(summary = "时间线聚合查询",
            description = "未登录仅返回官方图片；已登录额外返回当前用户的备忘(ugc)与笔记(comment)。")
    public ApiResponse<List<TimelineItemVO>> timeline(
            @RequestParam Integer startYear,
            @RequestParam Integer endYear,
            @RequestParam(required = false, defaultValue = "all") String type,
            @RequestParam(required = false) Long poiId,
            HttpServletRequest request) {
        if (startYear == null || endYear == null) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "startYear and endYear are required");
        }
        Long userId = userAuthInterceptor.resolveUserId(request);
        return ApiResponse.success(timelineService.list(userId, startYear, endYear, type, poiId));
    }
}
