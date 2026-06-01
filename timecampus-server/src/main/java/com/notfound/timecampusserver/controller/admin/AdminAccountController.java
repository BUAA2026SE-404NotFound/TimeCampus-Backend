package com.notfound.timecampusserver.controller.admin;

import com.notfound.timecampuscommon.api.ApiResponse;
import com.notfound.timecampuspojo.dto.AdminRoleUpdateRequest;
import com.notfound.timecampuspojo.dto.AdminStatusUpdateRequest;
import com.notfound.timecampuspojo.vo.AdminAccountVO;
import com.notfound.timecampusserver.security.AdminContext;
import com.notfound.timecampusserver.service.AdminAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Admin-Account", description = "管理员账户管理")
@RestController
@RequestMapping("/api/v1/admin/accounts")
public class AdminAccountController {

    private final AdminAccountService adminAccountService;

    public AdminAccountController(AdminAccountService adminAccountService) {
        this.adminAccountService = adminAccountService;
    }

    @GetMapping
    @Operation(summary = "管理员列表", description = "仅超级管理员可查看管理员账户列表。")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<AdminAccountVO>> listAccounts() {
        return ApiResponse.success(adminAccountService.listAccounts(AdminContext.getAdminId()));
    }

    @PostMapping("/{id}/status")
    @Operation(summary = "启用/禁用管理员", description = "仅超级管理员可启用或禁用普通管理员。")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<AdminAccountVO> updateStatus(@PathVariable Long id,
                                                    @Valid @RequestBody AdminStatusUpdateRequest request) {
        return ApiResponse.success(adminAccountService.updateStatus(id, Boolean.TRUE.equals(request.getEnabled()),
                AdminContext.getAdminId()));
    }

    @PostMapping("/{id}/role")
    @Operation(summary = "调整管理员角色", description = "仅超级管理员可调整角色（none/read/admin）；super 不能直接授予。")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<AdminAccountVO> updateRole(@PathVariable Long id,
                                                  @Valid @RequestBody AdminRoleUpdateRequest request) {
        return ApiResponse.success(adminAccountService.updateRole(id, request.getRole(), AdminContext.getAdminId()));
    }
}
