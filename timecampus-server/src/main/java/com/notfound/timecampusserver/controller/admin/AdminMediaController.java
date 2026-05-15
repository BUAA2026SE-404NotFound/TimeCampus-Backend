package com.notfound.timecampusserver.controller.admin;

import com.notfound.timecampuscommon.api.ApiResponse;
import com.notfound.timecampuspojo.dto.OfficialMediaImportRequest;
import com.notfound.timecampuspojo.dto.RejectRequest;
import com.notfound.timecampuspojo.vo.ImportResultVO;
import com.notfound.timecampuspojo.vo.MediaVO;
import com.notfound.timecampusserver.security.AdminContext;
import com.notfound.timecampusserver.service.AdminMediaService;
import com.notfound.timecampusserver.service.MediaFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Admin-Media", description = "管理员：影像（media）管理")
@RestController
@RequestMapping("/api/v1/admin/media")
public class AdminMediaController {

    private final AdminMediaService adminMediaService;
    private final MediaFileService mediaFileService;

    public AdminMediaController(AdminMediaService adminMediaService, MediaFileService mediaFileService) {
        this.adminMediaService = adminMediaService;
        this.mediaFileService = mediaFileService;
    }

    @PostMapping("/import")
    @Operation(summary = "批量导入官方影像", description = "导入到 media 表，type 固定为 official；reviewStatus 默认按 approved 处理。")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<ImportResultVO> importOfficial(@Valid @RequestBody OfficialMediaImportRequest request) {
        return ApiResponse.success(adminMediaService.importOfficial(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "按 ID 查询影像")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<MediaVO> getById(@Parameter(description = "影像 ID", example = "1") @PathVariable Long id) {
        return ApiResponse.success(adminMediaService.getById(id));
    }

    @GetMapping("/{id}/file")
    @Operation(summary = "读取本地影像文件", description = "用于管理端预览保存在文件系统中的 media.image_path。远程 URL 直接由前端访问。")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Resource> file(@Parameter(description = "影像 ID", example = "1") @PathVariable Long id) {
        Resource resource = mediaFileService.loadMediaFileAdmin(id);
        return ResponseEntity.ok()
                .header("Content-Type", mediaFileService.contentType(resource))
                .body(resource);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除影像")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Void> deleteById(@Parameter(description = "影像 ID", example = "1") @PathVariable Long id) {
        adminMediaService.deleteById(id);
        return ApiResponse.success();
    }

    @GetMapping
    @Operation(summary = "查询影像列表")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<MediaVO>> list(@Parameter(description = "地点 ID（可选）", example = "1")
                                          @RequestParam(required = false) Long poiId,
                                          @Parameter(description = "类型：official/ugc（可选）", example = "official")
                                          @RequestParam(required = false) String type,
                                          @Parameter(description = "审核状态：pending/approved/rejected（可选）", example = "approved")
                                          @RequestParam(required = false) String reviewStatus,
                                          @Parameter(description = "年份下界（可选）", example = "1950")
                                          @RequestParam(required = false) Integer yearFrom,
                                          @Parameter(description = "年份上界（可选）", example = "2020")
                                          @RequestParam(required = false) Integer yearTo) {
        return ApiResponse.success(adminMediaService.list(poiId, type, reviewStatus, yearFrom, yearTo));
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "审核通过影像", description = "管理员通过UGC影像的审核")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Void> approveMedia(@PathVariable Long id) {
        Long reviewerId = AdminContext.getAdminId();
        adminMediaService.approveMedia(id, reviewerId);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "审核驳回影像", description = "管理员驳回UGC影像，需提供驳回原因")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Void> rejectMedia(@PathVariable Long id,
                                         @RequestBody @Valid RejectRequest request) {
        Long reviewerId = AdminContext.getAdminId();
        adminMediaService.rejectMedia(id, reviewerId, request.getRejectReason());
        return ApiResponse.success();
    }
}
