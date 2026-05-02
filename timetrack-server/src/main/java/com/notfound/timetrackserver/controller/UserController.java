package com.notfound.timetrackserver.controller;

import com.notfound.timetrackcommon.api.ApiResponse;
import com.notfound.timetrackcommon.api.ResultCode;
import com.notfound.timetrackcommon.exception.BizException;
import com.notfound.timetrackpojo.dto.WechatLoginRequest;
import com.notfound.timetrackpojo.vo.UserLoginVO;
import com.notfound.timetrackpojo.vo.UserProfileVO;
import com.notfound.timetrackserver.security.UserContext;
import com.notfound.timetrackserver.service.UserService;
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

@Tag(name = "User", description = "用户相关接口")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/wx-login")
    @Operation(summary = "微信登录/注册", description = "使用小程序 wx.login 获取的 code 登录；若用户不存在则自动创建。")
    public ApiResponse<UserLoginVO> wxLogin(@Valid @RequestBody WechatLoginRequest request) {
        return ApiResponse.success(userService.wxLogin(request));
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
}
