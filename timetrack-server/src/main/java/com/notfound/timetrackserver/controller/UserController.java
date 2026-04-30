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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import com.notfound.timetrackpojo.dto.FavoriteRequest;
import com.notfound.timetrackpojo.vo.FavoriteItemVO;
import com.notfound.timetrackserver.service.FavoriteService;
import jakarta.validation.Valid;

import java.util.List;


@Tag(name = "User", description = "用户相关接口")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final FavoriteService favoriteService;

    public UserController(UserService userService, FavoriteService favoriteService) {
        this.userService = userService;
        this.favoriteService = favoriteService;
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

    @PostMapping("/favorites")
    @Operation(summary = "添加收藏", description = "收藏地点或影像")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Void> addFavorite(@Valid @RequestBody FavoriteRequest request) {
        Long userId = UserContext.getUserId();
        favoriteService.addFavorite(request, userId);
        return ApiResponse.success();
    }

    @DeleteMapping("/favorites")
    @Operation(summary = "取消收藏", description = "取消收藏地点或影像")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Void> removeFavorite(@Valid @RequestBody FavoriteRequest request) {
        Long userId = UserContext.getUserId();
        favoriteService.removeFavorite(request, userId);
        return ApiResponse.success();
    }

    @GetMapping("/favorites")
    @Operation(summary = "查询收藏列表", description = "可选的 targetType 过滤：poi 或 media")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<FavoriteItemVO>> listFavorites(
            @RequestParam(required = false) @Parameter(description = "目标类型：poi / media") String targetType) {
        Long userId = UserContext.getUserId();
        return ApiResponse.success(favoriteService.listFavorites(userId, targetType));
    }
}
