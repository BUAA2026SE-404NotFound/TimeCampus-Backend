package com.notfound.timetrackserver.controller.admin;

import com.notfound.timetrackcommon.api.ApiResponse;
import com.notfound.timetrackpojo.vo.MediaVO;
import com.notfound.timetrackserver.security.AdminContext;
import com.notfound.timetrackserver.service.UgcService;
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

@Tag(name = "Admin-UGC", description = "管理员：UGC 审核")
@RestController
@RequestMapping("/api/v1/admin/ugc")
public class AdminUgcController {

    private final UgcService ugcService;

    public AdminUgcController(UgcService ugcService) {
        this.ugcService = ugcService;
    }

    @GetMapping
    @Operation(summary = "查询 UGC 列表")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<MediaVO>> list(@RequestParam(defaultValue = "pending") String status) {
        return ApiResponse.success(ugcService.list(status));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "通过 UGC")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<MediaVO> approve(@PathVariable Long id) {
        return ApiResponse.success(ugcService.approve(id, AdminContext.getAdminId()));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "驳回 UGC")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<MediaVO> reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ApiResponse.success(ugcService.reject(id, body == null ? null : body.get("reason"), AdminContext.getAdminId()));
    }
}
