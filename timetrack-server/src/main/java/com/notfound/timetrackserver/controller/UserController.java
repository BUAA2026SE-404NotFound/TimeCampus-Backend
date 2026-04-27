package com.notfound.timetrackserver.controller;

import com.notfound.timetrackcommon.api.ApiResponse;
import com.notfound.timetrackpojo.dto.WechatLoginRequest;
import com.notfound.timetrackpojo.vo.UserProfileVO;
import com.notfound.timetrackserver.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/wx-login")
    public ApiResponse<UserProfileVO> wxLogin(@Valid @RequestBody WechatLoginRequest request) {
        return ApiResponse.success(userService.wxLogin(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserProfileVO> getUser(@PathVariable Long id) {
        return ApiResponse.success(userService.getById(id));
    }
}
