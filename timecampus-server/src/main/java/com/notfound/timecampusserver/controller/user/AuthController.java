package com.notfound.timecampusserver.controller.user;

import com.notfound.timecampuscommon.api.ApiResponse;
import com.notfound.timecampuspojo.dto.WechatLoginRequest;
import com.notfound.timecampuspojo.vo.UserLoginVO;
import com.notfound.timecampusserver.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "用户认证")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/wechat/login")
    @Operation(summary = "微信登录/注册")
    public ApiResponse<UserLoginVO> wechatLogin(@Valid @RequestBody WechatLoginRequest request) {
        return ApiResponse.success(userService.wxLogin(request));
    }
}
