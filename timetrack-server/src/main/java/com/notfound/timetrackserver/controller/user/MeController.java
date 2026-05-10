package com.notfound.timetrackserver.controller.user;

import com.notfound.timetrackcommon.api.ApiResponse;
import com.notfound.timetrackcommon.api.ResultCode;
import com.notfound.timetrackcommon.exception.BizException;
import com.notfound.timetrackpojo.vo.CommentVO;
import com.notfound.timetrackpojo.vo.MediaVO;
import com.notfound.timetrackpojo.vo.UserProfileVO;
import com.notfound.timetrackpojo.vo.UserReviewResultVO;
import com.notfound.timetrackserver.security.UserContext;
import com.notfound.timetrackserver.service.ReviewResultService;
import com.notfound.timetrackserver.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Me", description = "当前用户")
@RestController
@RequestMapping("/api/v1/me")
public class MeController {

    private final UserService userService;
    private final ReviewResultService reviewResultService;

    public MeController(UserService userService, ReviewResultService reviewResultService) {
        this.userService = userService;
        this.reviewResultService = reviewResultService;
    }

    @GetMapping
    @Operation(summary = "查询当前用户")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<UserProfileVO> me() {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "missing user token");
        }
        return ApiResponse.success(userService.getById(currentUserId));
    }

    @GetMapping("/review-results")
    @Operation(summary = "查询当前用户审核结果", description = "返回当前用户提交的 UGC 与评论审核结果，可用 status 过滤 pending/approved/rejected。")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<UserReviewResultVO> reviewResults(@RequestParam(required = false) String status) {
        Long currentUserId = requireCurrentUserId();
        return ApiResponse.success(reviewResultService.listByUser(currentUserId, status));
    }

    @GetMapping("/review-results/ugc")
    @Operation(summary = "查询当前用户 UGC 审核结果", description = "仅返回当前用户上传的 UGC 影像审核结果，可用 status 过滤 pending/approved/rejected。")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<MediaVO>> ugcReviewResults(@RequestParam(required = false) String status) {
        return ApiResponse.success(reviewResultService.listUgcByUser(requireCurrentUserId(), status));
    }

    @GetMapping("/review-results/comments")
    @Operation(summary = "查询当前用户评论审核结果", description = "仅返回当前用户评论审核结果，可用 status 过滤 pending/approved/rejected。")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<CommentVO>> commentReviewResults(@RequestParam(required = false) String status) {
        return ApiResponse.success(reviewResultService.listCommentsByUser(requireCurrentUserId(), status));
    }

    private Long requireCurrentUserId() {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "missing user token");
        }
        return currentUserId;
    }
}
