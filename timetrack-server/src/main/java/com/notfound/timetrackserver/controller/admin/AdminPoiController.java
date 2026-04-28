package com.notfound.timetrackserver.controller.admin;

import com.notfound.timetrackcommon.api.ApiResponse;
import com.notfound.timetrackpojo.dto.PoiCreateRequest;
import com.notfound.timetrackpojo.dto.PoiUpdateRequest;
import com.notfound.timetrackpojo.vo.PoiVO;
import com.notfound.timetrackserver.service.PoiService;
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

import java.util.List;

@Tag(name = "Admin-POI", description = "管理员：地点（POI）管理")
@RestController
@RequestMapping("/api/v1/admin/pois")
public class AdminPoiController {

    private final PoiService poiService;

    public AdminPoiController(PoiService poiService) {
        this.poiService = poiService;
    }

    @PostMapping
    @Operation(summary = "新增 POI")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<PoiVO> create(@Valid @RequestBody PoiCreateRequest request) {
        return ApiResponse.success(poiService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新 POI")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<PoiVO> update(@Parameter(description = "POI ID", example = "1") @PathVariable Long id,
                                     @Valid @RequestBody PoiUpdateRequest request) {
        return ApiResponse.success(poiService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除 POI")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Void> delete(@Parameter(description = "POI ID", example = "1") @PathVariable Long id) {
        poiService.delete(id);
        return ApiResponse.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询 POI 详情")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<PoiVO> getById(@Parameter(description = "POI ID", example = "1") @PathVariable Long id) {
        return ApiResponse.success(poiService.getById(id));
    }

    @GetMapping
    @Operation(summary = "查询 POI 列表")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<PoiVO>> list(@Parameter(description = "上架状态：1 上架，0 下架（可选）", example = "1")
                                         @RequestParam(required = false) Integer status,
                                         @Parameter(description = "名称关键字（可选，模糊匹配）", example = "主楼")
                                         @RequestParam(required = false) String keyword) {
        return ApiResponse.success(poiService.list(status, keyword));
    }
}

