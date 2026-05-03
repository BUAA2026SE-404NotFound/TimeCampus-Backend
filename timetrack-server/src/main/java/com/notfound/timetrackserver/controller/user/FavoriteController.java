package com.notfound.timetrackserver.controller.user;

import com.notfound.timetrackcommon.api.ApiResponse;
import com.notfound.timetrackpojo.dto.FavoriteRequest;
import com.notfound.timetrackpojo.vo.FavoriteItemVO;
import com.notfound.timetrackserver.security.UserContext;
import com.notfound.timetrackserver.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Favorite", description = "收藏")
@RestController
@RequestMapping("/api/v1/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/{targetType}/{targetId}")
    @Operation(summary = "添加收藏")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Void> add(@PathVariable String targetType, @PathVariable Long targetId) {
        favoriteService.addFavorite(toRequest(targetType, targetId), UserContext.getUserId());
        return ApiResponse.success();
    }

    @DeleteMapping("/{targetType}/{targetId}")
    @Operation(summary = "取消收藏")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Void> remove(@PathVariable String targetType, @PathVariable Long targetId) {
        favoriteService.removeFavorite(toRequest(targetType, targetId), UserContext.getUserId());
        return ApiResponse.success();
    }

    @GetMapping
    @Operation(summary = "查询收藏列表")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<FavoriteItemVO>> list() {
        return ApiResponse.success(favoriteService.listFavorites(UserContext.getUserId(), null));
    }

    private FavoriteRequest toRequest(String targetType, Long targetId) {
        FavoriteRequest request = new FavoriteRequest();
        request.setTargetType(targetType);
        request.setTargetId(targetId);
        return request;
    }
}
