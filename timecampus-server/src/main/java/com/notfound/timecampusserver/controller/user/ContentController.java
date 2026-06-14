package com.notfound.timecampusserver.controller.user;

import com.notfound.timecampuscommon.api.ApiResponse;
import com.notfound.timecampuscommon.api.ResultCode;
import com.notfound.timecampuscommon.exception.BizException;
import com.notfound.timecampuspojo.entity.MediaEntity;
import com.notfound.timecampuspojo.vo.MediaVO;
import com.notfound.timecampusserver.mapper.MediaMapper;
import com.notfound.timecampusserver.service.impl.MediaStructMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Content", description = "公开内容查询")
@RestController
@RequestMapping("/api/v1")
public class ContentController {

    private static final String REVIEW_APPROVED = "approved";

    private final MediaMapper mediaMapper;
    private final MediaStructMapper mediaStructMapper;

    public ContentController(MediaMapper mediaMapper, MediaStructMapper mediaStructMapper) {
        this.mediaMapper = mediaMapper;
        this.mediaStructMapper = mediaStructMapper;
    }

    @GetMapping("/pois/{id}/contents")
    @Operation(summary = "查询 POI 下已审核通过内容")
    public ApiResponse<List<MediaVO>> listByPoi(@PathVariable Long id,
                                                @RequestParam(required = false) String type,
                                                @RequestParam(required = false) Integer yearFrom,
                                                @RequestParam(required = false) Integer yearTo) {
        return ApiResponse.success(listApprovedByPoi(id, type, yearFrom, yearTo));
    }

    @GetMapping("/pois/{id}/official-contents")
    @Operation(summary = "查询 POI 下官方已审核通过内容", description = "兼容旧前端路径；等价于 /pois/{id}/contents?type=official。")
    public ApiResponse<List<MediaVO>> listOfficialByPoi(@PathVariable Long id,
                                                       @RequestParam(required = false) Integer yearFrom,
                                                       @RequestParam(required = false) Integer yearTo) {
        return ApiResponse.success(listApprovedByPoi(id, "official", yearFrom, yearTo));
    }

    private List<MediaVO> listApprovedByPoi(Long id, String type, Integer yearFrom, Integer yearTo) {
        return mediaMapper.list(id, normalizeType(type), REVIEW_APPROVED, yearFrom, yearTo)
                .stream()
                .map(mediaStructMapper::toVO)
                .toList();
    }

    @GetMapping("/contents/{id}")
    @Operation(summary = "查询已审核通过内容详情")
    public ApiResponse<MediaVO> getById(@PathVariable Long id) {
        MediaEntity entity = mediaMapper.findById(id);
        if (entity == null || !REVIEW_APPROVED.equals(entity.getReviewStatus())) {
            throw new BizException(ResultCode.NOT_FOUND, "content not found: " + id);
        }
        return ApiResponse.success(mediaStructMapper.toVO(entity));
    }

    @GetMapping("/pois/{id}/time-switch")
    @Operation(summary = "按年份切换 POI 内容")
    public ApiResponse<MediaVO> timeSwitch(@PathVariable Long id, @RequestParam Integer year) {
        MediaEntity entity = mediaMapper.findBestByPoiAndYear(id, year, null, REVIEW_APPROVED);
        if (entity == null) {
            throw new BizException(ResultCode.NOT_FOUND, "content not found for poi: " + id);
        }
        return ApiResponse.success(mediaStructMapper.toVO(entity));
    }

    private String normalizeType(String type) {
        return type == null || type.isBlank() ? null : type.toLowerCase();
    }
}
