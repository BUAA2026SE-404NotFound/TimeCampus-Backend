package com.notfound.timecampusserver.controller.user;

import com.notfound.timecampuscommon.api.ApiResponse;
import com.notfound.timecampuspojo.dto.CommentCreateRequest;
import com.notfound.timecampuspojo.vo.CommentVO;
import com.notfound.timecampusserver.security.UserContext;
import com.notfound.timecampusserver.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Comment", description = "用户评论")
@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    @Operation(summary = "发表评论", description = "评论提交后进入待审核状态。")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<CommentVO> create(@Valid @RequestBody CommentCreateRequest request) {
        return ApiResponse.success(commentService.create(request, UserContext.getUserId()));
    }

    @GetMapping("/{targetType}/{targetId}")
    @Operation(summary = "查询目标公开评论", description = "只返回已审核通过的评论。")
    public ApiResponse<List<CommentVO>> listByTarget(@PathVariable String targetType, @PathVariable Long targetId) {
        return ApiResponse.success(commentService.listByTarget(targetType, targetId));
    }

    @GetMapping("/mine")
    @Operation(summary = "查询我的评论", description = "登录用户查看自己的评论，可按审核状态过滤。")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<CommentVO>> mine(@RequestParam(required = false) String status) {
        return ApiResponse.success(commentService.listMine(UserContext.getUserId(), status));
    }
}
