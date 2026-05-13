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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import com.notfound.timetrackpojo.dto.FavoriteRequest;
import com.notfound.timetrackpojo.vo.FavoriteItemVO;
import com.notfound.timetrackserver.service.FavoriteService;

import java.util.List;


@Tag(name = "User", description = "用户相关接口")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final FavoriteService favoriteService;
    private final ReviewResultService reviewResultService;

    public UserController(UserService userService,
                          FavoriteService favoriteService,
                          ReviewResultService reviewResultService) {
        this.userService = userService;
        this.favoriteService = favoriteService;
        this.reviewResultService = reviewResultService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "按 ID 查询用户")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<UserProfileVO> getUser(@PathVariable Long id) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId != null && !currentUserId.equals(id)) {
            throw new BizException(ResultCode.FORBIDDEN, "forbidden");
        }
        return ApiResponse.success(userService.getById(id));
    }

    @GetMapping("/{id}/review-results")
    @Operation(summary = "按用户 ID 查询审核结果", description = "仅允许查询当前登录用户提交的 UGC 与评论审核结果，可用 status 过滤 pending/approved/rejected。")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<UserReviewResultVO> reviewResults(@PathVariable Long id,
                                                         @RequestParam(required = false) String status) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "missing user token");
        }
        if (!currentUserId.equals(id)) {
            throw new BizException(ResultCode.FORBIDDEN, "forbidden");
        }
        return ApiResponse.success(reviewResultService.listByUser(id, status));
    }

    @GetMapping("/{id}/review-results/ugc")
    @Operation(summary = "按用户 ID 查询 UGC 审核结果", description = "仅允许查询当前登录用户上传的 UGC 影像审核结果，可用 status 过滤 pending/approved/rejected。")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<MediaVO>> ugcReviewResults(@PathVariable Long id,
                                                       @RequestParam(required = false) String status) {
        requireSameUser(id);
        return ApiResponse.success(reviewResultService.listUgcByUser(id, status));
    }

    @GetMapping("/{id}/review-results/comments")
    @Operation(summary = "按用户 ID 查询评论审核结果", description = "仅允许查询当前登录用户发布的评论审核结果，可用 status 过滤 pending/approved/rejected。")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<CommentVO>> commentReviewResults(@PathVariable Long id,
                                                             @RequestParam(required = false) String status) {
        requireSameUser(id);
        return ApiResponse.success(reviewResultService.listCommentsByUser(id, status));
    }

    @PostMapping("/favorites")
    @Operation(summary = "添加收藏", description = "收藏地点或影像")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Void> addFavorite(@Valid @RequestBody FavoriteRequest request) {
        Long userId = UserContext.getUserId();
        favoriteService.addFavorite(request, userId);
        return ApiResponse.success();
    }

    @DeleteMapping("/favorites")
    @Operation(summary = "取消收藏", description = "取消收藏地点或影像")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Void> removeFavorite(@Valid @RequestBody FavoriteRequest request) {
        Long userId = UserContext.getUserId();
        favoriteService.removeFavorite(request, userId);
        return ApiResponse.success();
    }

    @GetMapping("/favorites")
    @Operation(summary = "查询收藏列表", description = "可选的 targetType 过滤：poi 或 media")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<FavoriteItemVO>> listFavorites(
            @RequestParam(required = false) @Parameter(description = "目标类型：poi / media") String targetType) {
        Long userId = UserContext.getUserId();
        return ApiResponse.success(favoriteService.listFavorites(userId, targetType));
    }

    private void requireSameUser(Long id) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "missing user token");
        }
        if (!currentUserId.equals(id)) {
            throw new BizException(ResultCode.FORBIDDEN, "forbidden");
        }
    }
}
