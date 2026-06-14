package com.notfound.timecampusserver.controller.admin;

import com.notfound.timecampuscommon.api.ApiResponse;
import com.notfound.timecampuspojo.vo.CommentVO;
import com.notfound.timecampusserver.security.AdminContext;
import com.notfound.timecampusserver.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "Admin-Comment", description = "管理员：评论审核")
@RestController
@RequestMapping("/api/v1/admin/comments")
public class AdminCommentController {

    private final CommentService commentService;

    public AdminCommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    @Operation(summary = "查询评论列表")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<CommentVO>> list(@RequestParam(required = false) String targetType,
                                             @RequestParam(required = false) Long targetId,
                                             @RequestParam(required = false) String status) {
        return ApiResponse.success(commentService.listForAdmin(targetType, targetId, status));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "通过评论")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<CommentVO> approve(@PathVariable Long id) {
        return ApiResponse.success(commentService.approve(id, AdminContext.getAdminId()));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "驳回评论")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<CommentVO> reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ApiResponse.success(commentService.reject(id, body == null ? null : body.get("reason"), AdminContext.getAdminId()));
    }
}
