package com.notfound.timecampusserver.controller.user;

import com.notfound.timecampuscommon.api.ApiResponse;
import com.notfound.timecampuscommon.api.ResultCode;
import com.notfound.timecampuscommon.exception.BizException;
import com.notfound.timecampuspojo.dto.CreateNoteRequest;
import com.notfound.timecampuspojo.vo.*;
import com.notfound.timecampusserver.security.UserContext;
import com.notfound.timecampusserver.service.CommentService;
import com.notfound.timecampusserver.service.MemoService;
import com.notfound.timecampusserver.service.ReviewResultService;
import com.notfound.timecampusserver.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Me", description = "当前用户")
@RestController
@RequestMapping("/api/v1/me")
public class MeController {

    private final UserService userService;
    private final ReviewResultService reviewResultService;

    @Autowired
    private CommentService commentService;
    @Autowired
    private MemoService memoService;

    public MeController(UserService userService, ReviewResultService reviewResultService) {
        this.userService = userService;
        this.reviewResultService = reviewResultService;
    }

    @PostMapping("/notes")
    @Operation(summary = "创建私有笔记")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<NoteVO> createNote(@Valid @RequestBody CreateNoteRequest request) {
        Long userId = UserContext.getUserId();
        return ApiResponse.success(commentService.createNote(userId, request.getPoiId(), request.getContent()));
    }

    @GetMapping("/notes")
    @Operation(summary = "私有笔记列表")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<NoteVO>> listMyNotes(@RequestParam(required = false) Long poiId,
                                                 @RequestParam(required = false) String status) {
        Long userId = UserContext.getUserId();
        List<NoteVO> list = commentService.listMyNotes(userId, poiId, status);
        return ApiResponse.success(list);
    }

    @DeleteMapping("/notes/{id}")
    @Operation(summary = "删除私有笔记")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Void> deleteNote(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        commentService.deleteNote(id, userId);
        return ApiResponse.success();
    }

    @PostMapping("/memos")
    @Operation(summary = "创建私有图片备忘")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<MediaVO> createMemo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("poiId") Long poiId,
            @RequestParam("year") Integer year,
            @RequestParam(required = false) String description) {
        Long userId = UserContext.getUserId();
        return ApiResponse.success(memoService.createMemo(userId, poiId, year, description, file));
    }

    @GetMapping("/memos")
    @Operation(summary = "查询私有图片备忘列表")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<MediaVO>> listMemos(
            @RequestParam(required = false) Long poiId,
            @RequestParam(required = false) Integer yearFrom,
            @RequestParam(required = false) Integer yearTo) {
        Long userId = UserContext.getUserId();
        List<MediaVO> memos = memoService.listMemos(userId, poiId, yearFrom, yearTo);
        return ApiResponse.success(memos);
    }

    @DeleteMapping("/memos/{id}")
    @Operation(summary = "删除私有图片备忘")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Void> deleteMemo(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        memoService.deleteMemo(id, userId);
        return ApiResponse.success();
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
