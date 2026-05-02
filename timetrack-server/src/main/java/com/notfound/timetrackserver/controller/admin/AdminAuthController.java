package com.notfound.timetrackserver.controller.admin;

import com.notfound.timetrackcommon.api.ApiResponse;
import com.notfound.timetrackpojo.dto.AdminLoginRequest;
import com.notfound.timetrackpojo.vo.AdminLoginVO;
import com.notfound.timetrackserver.service.AdminAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin-Auth", description = "管理员登录与会话")
@RestController
@RequestMapping("/api/v1/admin")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/login")
    @Operation(summary = "管理员登录", description = "登录成功后返回 token；后续访问 /api/v1/admin/** 需要在 Authorization 中携带 Bearer token。")
    public ApiResponse<AdminLoginVO> login(@Valid @RequestBody AdminLoginRequest request) {
        return ApiResponse.success(adminAuthService.login(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "管理员登出", description = "使当前 token 失效。")
    public ApiResponse<Void> logout(@Parameter(description = "Authorization: Bearer <token>", required = true)
                                    @RequestHeader(name = "Authorization", required = false) String authorization) {
        String token = extractBearer(authorization);
        adminAuthService.logout(token);
        return ApiResponse.success();
    }

    private String extractBearer(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            return null;
        }
        String prefix = "Bearer ";
        if (!authorization.startsWith(prefix)) {
            return null;
        }
        String token = authorization.substring(prefix.length()).trim();
        return token.isBlank() ? null : token;
    }
}

