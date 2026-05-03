package com.notfound.timetrackserver.controller.user;

import com.notfound.timetrackcommon.api.ApiResponse;
import com.notfound.timetrackcommon.api.ResultCode;
import com.notfound.timetrackcommon.exception.BizException;
import com.notfound.timetrackpojo.vo.MediaVO;
import com.notfound.timetrackserver.security.UserContext;
import com.notfound.timetrackserver.service.UgcService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "UGC", description = "用户生成内容")
@RestController
@RequestMapping("/api/v1/ugc")
public class UgcController {

    private final UgcService ugcService;

    public UgcController(UgcService ugcService) {
        this.ugcService = ugcService;
    }

    @PostMapping
    @Operation(summary = "上传 UGC")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<MediaVO> upload(@RequestPart MultipartFile file,
                                       @RequestParam Long poiId,
                                       @RequestParam Integer year,
                                       @RequestParam(required = false) String description,
                                       @RequestParam(required = false) String source) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "missing user token");
        }
        return ApiResponse.success(ugcService.upload(file, poiId, year, description, source, userId));
    }
}
