package com.notfound.timecampusserver.controller.admin;

import com.notfound.timecampuscommon.api.ApiResponse;
import com.notfound.timecampuspojo.dto.OfficialMediaImportRequest;
import com.notfound.timecampuspojo.vo.ImportResultVO;
import com.notfound.timecampusserver.service.AdminMediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin-Content", description = "管理员：官方内容")
@RestController
@RequestMapping("/api/v1/admin/contents")
public class AdminContentController {

    private final AdminMediaService adminMediaService;

    public AdminContentController(AdminMediaService adminMediaService) {
        this.adminMediaService = adminMediaService;
    }

    @PostMapping("/batch-import")
    @Operation(summary = "批量导入官方内容", description = "Alpha 标准路径，内部落到 media 表。")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<ImportResultVO> batchImport(@Valid @RequestBody OfficialMediaImportRequest request) {
        return ApiResponse.success(adminMediaService.importOfficial(request));
    }
}
