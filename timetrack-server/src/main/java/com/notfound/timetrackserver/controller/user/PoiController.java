package com.notfound.timetrackserver.controller.user;

import com.notfound.timetrackcommon.api.ApiResponse;
import com.notfound.timetrackpojo.vo.PoiVO;
import com.notfound.timetrackserver.service.PoiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "POI", description = "公开 POI 查询")
@RestController
@RequestMapping("/api/v1/pois")
public class PoiController {

    private final PoiService poiService;

    public PoiController(PoiService poiService) {
        this.poiService = poiService;
    }

    @GetMapping
    @Operation(summary = "查询公开 POI 列表")
    public ApiResponse<List<PoiVO>> list(@RequestParam(required = false) String keyword) {
        return ApiResponse.success(poiService.list(1, keyword));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询公开 POI 详情")
    public ApiResponse<PoiVO> getById(@PathVariable Long id) {
        return ApiResponse.success(poiService.getById(id));
    }
}
