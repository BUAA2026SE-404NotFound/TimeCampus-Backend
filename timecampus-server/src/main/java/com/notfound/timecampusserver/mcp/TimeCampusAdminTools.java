package com.notfound.timecampusserver.mcp;

import com.notfound.timecampuscommon.api.ResultCode;
import com.notfound.timecampuscommon.exception.BizException;
import com.notfound.timecampuspojo.dto.MediaMetadataUpdateRequest;
import com.notfound.timecampuspojo.dto.OfficialMediaImportRequest;
import com.notfound.timecampuspojo.dto.PoiCreateRequest;
import com.notfound.timecampuspojo.dto.PoiUpdateRequest;
import com.notfound.timecampuspojo.vo.ImportResultVO;
import com.notfound.timecampuspojo.vo.MediaVO;
import com.notfound.timecampuspojo.vo.PoiVO;
import com.notfound.timecampusserver.security.AdminContext;
import com.notfound.timecampusserver.service.AdminMediaService;
import com.notfound.timecampusserver.service.PoiService;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class TimeCampusAdminTools {

    private final PoiService poiService;
    private final AdminMediaService adminMediaService;
    private final TimeCampusMcpAdminScope adminScope;

    public TimeCampusAdminTools(PoiService poiService,
                                AdminMediaService adminMediaService,
                                TimeCampusMcpAdminScope adminScope) {
        this.poiService = poiService;
        this.adminMediaService = adminMediaService;
        this.adminScope = adminScope;
    }

    @McpTool(
            name = "timecampus_search_pois",
            description = "搜索时光航迹 POI。默认只返回上架 POI；需要维护下架内容时传 includeOffline=true。",
            annotations = @McpTool.McpAnnotations(
                    title = "Search TimeCampus POIs",
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false))
    public List<PoiVO> searchPois(
            @McpToolParam(description = "POI 名称关键字，支持模糊匹配；为空返回列表", required = false)
            String keyword,
            @McpToolParam(description = "状态：1 上架，0 下架；为空时由 includeOffline 决定", required = false)
            Integer status,
            @McpToolParam(description = "是否包含下架 POI；false 且 status 为空时默认只查上架", required = false)
            Boolean includeOffline) {
        return adminScope.call(() -> poiService.list(resolvePoiStatus(status, includeOffline), keyword));
    }

    @McpTool(
            name = "timecampus_get_poi",
            description = "按 ID 获取 POI 详情，用于编辑前确认当前名称、坐标、简介、冷知识和上下架状态。",
            annotations = @McpTool.McpAnnotations(
                    title = "Get TimeCampus POI",
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false))
    public PoiVO getPoi(
            @McpToolParam(description = "POI ID", required = true) Long poiId) {
        return adminScope.call(() -> poiService.getById(poiId));
    }

    @McpTool(
            name = "timecampus_create_poi",
            description = "新增 POI。适合 agent 在确认地点名称、坐标和文案后创建新地点。",
            annotations = @McpTool.McpAnnotations(
                    title = "Create TimeCampus POI",
                    readOnlyHint = false,
                    destructiveHint = false,
                    idempotentHint = false,
                    openWorldHint = false))
    public PoiVO createPoi(
            @McpToolParam(description = "地点名称，不能为空", required = true) String name,
            @McpToolParam(description = "纬度，范围 -90 到 90", required = true) BigDecimal latitude,
            @McpToolParam(description = "经度，范围 -180 到 180", required = true) BigDecimal longitude,
            @McpToolParam(description = "地点简介，可直接作为网站文案", required = false) String description,
            @McpToolParam(description = "冷知识/小故事，可为空", required = false) String funFact,
            @McpToolParam(description = "上架状态：1 上架，0 下架；为空默认上架", required = false) Integer status) {
        return adminScope.call(() -> {
            validatePoiFields(name, latitude, longitude, status);
            PoiCreateRequest request = new PoiCreateRequest();
            request.setName(name);
            request.setLatitude(latitude);
            request.setLongitude(longitude);
            request.setDescription(description);
            request.setFunFact(funFact);
            request.setStatus(status);
            return poiService.create(request);
        });
    }

    @McpTool(
            name = "timecampus_update_poi",
            description = "更新 POI 基础信息。未传字段会沿用当前值，适合局部修改坐标、名称、简介、冷知识或上下架状态。",
            annotations = @McpTool.McpAnnotations(
                    title = "Update TimeCampus POI",
                    readOnlyHint = false,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false))
    public PoiVO updatePoi(
            @McpToolParam(description = "POI ID", required = true) Long poiId,
            @McpToolParam(description = "新地点名称；为空则保持不变", required = false) String name,
            @McpToolParam(description = "新纬度；为空则保持不变", required = false) BigDecimal latitude,
            @McpToolParam(description = "新经度；为空则保持不变", required = false) BigDecimal longitude,
            @McpToolParam(description = "新地点简介；传空字符串可清空", required = false) String description,
            @McpToolParam(description = "新冷知识/小故事；传空字符串可清空", required = false) String funFact,
            @McpToolParam(description = "上架状态：1 上架，0 下架；为空则保持不变", required = false) Integer status) {
        return adminScope.call(() -> {
            PoiVO current = poiService.getById(poiId);
            PoiUpdateRequest request = new PoiUpdateRequest();
            request.setName(name == null ? current.getName() : name);
            request.setLatitude(latitude == null ? current.getLatitude() : latitude);
            request.setLongitude(longitude == null ? current.getLongitude() : longitude);
            request.setDescription(description == null ? current.getDescription() : description);
            request.setFunFact(funFact == null ? current.getFunFact() : funFact);
            request.setStatus(status == null ? current.getStatus() : status);
            validatePoiFields(request.getName(), request.getLatitude(), request.getLongitude(), request.getStatus());
            return poiService.update(poiId, request);
        });
    }

    @McpTool(
            name = "timecampus_update_poi_copy",
            description = "只更新 POI 的展示文案：地点简介和冷知识。适合网站/小程序文案快速润色。",
            annotations = @McpTool.McpAnnotations(
                    title = "Update POI Copy",
                    readOnlyHint = false,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false))
    public PoiVO updatePoiCopy(
            @McpToolParam(description = "POI ID", required = true) Long poiId,
            @McpToolParam(description = "新的地点简介；传空字符串可清空", required = true) String description,
            @McpToolParam(description = "新的冷知识/小故事；传空字符串可清空", required = false) String funFact) {
        return updatePoi(poiId, null, null, null, description, funFact, null);
    }

    @McpTool(
            name = "timecampus_delete_poi",
            description = "删除 POI。删除前应先读取 POI 与关联影像，确认没有误删。",
            annotations = @McpTool.McpAnnotations(
                    title = "Delete TimeCampus POI",
                    readOnlyHint = false,
                    destructiveHint = true,
                    idempotentHint = false,
                    openWorldHint = false))
    public OperationResult deletePoi(
            @McpToolParam(description = "POI ID", required = true) Long poiId,
            @McpToolParam(description = "必须传 true 才会执行删除", required = true) Boolean confirmDelete) {
        return adminScope.call(() -> {
            requireConfirmation(confirmDelete, "confirmDelete must be true to delete poi");
            poiService.delete(poiId);
            return new OperationResult("deleted", "poi", poiId, "POI deleted");
        });
    }

    @McpTool(
            name = "timecampus_list_media",
            description = "查询影像资料。可按 POI、类型、审核状态和年份范围过滤，用于维护影像库和编辑影像说明。",
            annotations = @McpTool.McpAnnotations(
                    title = "List TimeCampus Media",
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false))
    public List<MediaVO> listMedia(
            @McpToolParam(description = "POI ID，可为空", required = false) Long poiId,
            @McpToolParam(description = "类型：official/ugc/memo，可为空", required = false) String type,
            @McpToolParam(description = "审核状态：pending/approved/rejected，可为空", required = false) String reviewStatus,
            @McpToolParam(description = "年份下界，可为空", required = false) Integer yearFrom,
            @McpToolParam(description = "年份上界，可为空", required = false) Integer yearTo) {
        return adminScope.call(() -> adminMediaService.list(poiId, type, reviewStatus, yearFrom, yearTo));
    }

    @McpTool(
            name = "timecampus_get_media",
            description = "按 ID 获取影像资料详情，用于编辑前确认当前图片路径、年份、说明、审核状态和关联 POI。",
            annotations = @McpTool.McpAnnotations(
                    title = "Get TimeCampus Media",
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false))
    public MediaVO getMedia(
            @McpToolParam(description = "影像 ID", required = true) Long mediaId) {
        return adminScope.call(() -> adminMediaService.getById(mediaId));
    }

    @McpTool(
            name = "timecampus_import_official_media",
            description = "批量导入官方历史影像记录。每条记录需包含 poiId、imagePath、year，可选 description/reviewStatus。",
            annotations = @McpTool.McpAnnotations(
                    title = "Import Official Media",
                    readOnlyHint = false,
                    destructiveHint = false,
                    idempotentHint = false,
                    openWorldHint = false))
    public ImportResultVO importOfficialMedia(
            @McpToolParam(description = "官方影像条目列表", required = true) List<OfficialMediaDraft> items) {
        return adminScope.call(() -> {
            if (items == null || items.isEmpty()) {
                throw new BizException(ResultCode.VALIDATION_ERROR, "items cannot be empty");
            }
            OfficialMediaImportRequest request = new OfficialMediaImportRequest();
            request.setItems(items.stream().map(this::toOfficialMediaItem).toList());
            return adminMediaService.importOfficial(request);
        });
    }

    @McpTool(
            name = "timecampus_update_media_metadata",
            description = "更新影像元数据。未传字段保持不变；适合修正关联 POI、图片路径、年份、说明或审核状态。",
            annotations = @McpTool.McpAnnotations(
                    title = "Update Media Metadata",
                    readOnlyHint = false,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false))
    public MediaVO updateMediaMetadata(
            @McpToolParam(description = "影像 ID", required = true) Long mediaId,
            @McpToolParam(description = "新的关联 POI ID；为空则保持不变", required = false) Long poiId,
            @McpToolParam(description = "新的图片路径或 URL；为空则保持不变", required = false) String imagePath,
            @McpToolParam(description = "新的拍摄年份；为空则保持不变", required = false) Integer year,
            @McpToolParam(description = "新的影像说明；传空字符串可清空", required = false) String description,
            @McpToolParam(description = "审核状态：pending/approved/rejected；为空则保持不变", required = false) String reviewStatus) {
        return adminScope.call(() -> {
            MediaMetadataUpdateRequest request = new MediaMetadataUpdateRequest();
            request.setPoiId(poiId);
            request.setImagePath(imagePath);
            request.setYear(year);
            request.setDescription(description);
            request.setReviewStatus(reviewStatus);
            return adminMediaService.updateMetadata(mediaId, request, AdminContext.getAdminId());
        });
    }

    @McpTool(
            name = "timecampus_update_media_copy",
            description = "只更新影像说明文案。适合快速润色图片说明，不改变图片路径、年份或审核状态。",
            annotations = @McpTool.McpAnnotations(
                    title = "Update Media Copy",
                    readOnlyHint = false,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false))
    public MediaVO updateMediaCopy(
            @McpToolParam(description = "影像 ID", required = true) Long mediaId,
            @McpToolParam(description = "新的影像说明；传空字符串可清空", required = true) String description) {
        return updateMediaMetadata(mediaId, null, null, null, description, null);
    }

    @McpTool(
            name = "timecampus_delete_media",
            description = "删除影像资料。删除前应先读取影像详情，确认不是误删。",
            annotations = @McpTool.McpAnnotations(
                    title = "Delete TimeCampus Media",
                    readOnlyHint = false,
                    destructiveHint = true,
                    idempotentHint = false,
                    openWorldHint = false))
    public OperationResult deleteMedia(
            @McpToolParam(description = "影像 ID", required = true) Long mediaId,
            @McpToolParam(description = "必须传 true 才会执行删除", required = true) Boolean confirmDelete) {
        return adminScope.call(() -> {
            requireConfirmation(confirmDelete, "confirmDelete must be true to delete media");
            adminMediaService.deleteById(mediaId);
            return new OperationResult("deleted", "media", mediaId, "Media deleted");
        });
    }

    @McpTool(
            name = "timecampus_approve_media",
            description = "审核通过 pending 状态的 UGC 影像。",
            annotations = @McpTool.McpAnnotations(
                    title = "Approve Media",
                    readOnlyHint = false,
                    destructiveHint = false,
                    idempotentHint = false,
                    openWorldHint = false))
    public OperationResult approveMedia(
            @McpToolParam(description = "影像 ID", required = true) Long mediaId) {
        return adminScope.call(() -> {
            adminMediaService.approveMedia(mediaId, AdminContext.getAdminId());
            return new OperationResult("approved", "media", mediaId, "Media approved");
        });
    }

    @McpTool(
            name = "timecampus_reject_media",
            description = "驳回 pending 状态的 UGC 影像，并记录驳回原因。",
            annotations = @McpTool.McpAnnotations(
                    title = "Reject Media",
                    readOnlyHint = false,
                    destructiveHint = true,
                    idempotentHint = false,
                    openWorldHint = false))
    public OperationResult rejectMedia(
            @McpToolParam(description = "影像 ID", required = true) Long mediaId,
            @McpToolParam(description = "驳回原因，不能为空", required = true) String rejectReason) {
        return adminScope.call(() -> {
            adminMediaService.rejectMedia(mediaId, AdminContext.getAdminId(), rejectReason);
            return new OperationResult("rejected", "media", mediaId, "Media rejected");
        });
    }

    private Integer resolvePoiStatus(Integer status, Boolean includeOffline) {
        if (status != null) {
            validateStatus(status);
            return status;
        }
        return Boolean.TRUE.equals(includeOffline) ? null : 1;
    }

    private void validatePoiFields(String name, BigDecimal latitude, BigDecimal longitude, Integer status) {
        if (name == null || name.isBlank()) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "name cannot be blank");
        }
        if (latitude == null || latitude.compareTo(BigDecimal.valueOf(-90)) < 0
                || latitude.compareTo(BigDecimal.valueOf(90)) > 0) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "latitude must be between -90 and 90");
        }
        if (longitude == null || longitude.compareTo(BigDecimal.valueOf(-180)) < 0
                || longitude.compareTo(BigDecimal.valueOf(180)) > 0) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "longitude must be between -180 and 180");
        }
        validateStatus(status);
    }

    private void validateStatus(Integer status) {
        if (status != null && status != 0 && status != 1) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "status must be 0 or 1");
        }
    }

    private void requireConfirmation(Boolean confirmDelete, String message) {
        if (!Boolean.TRUE.equals(confirmDelete)) {
            throw new BizException(ResultCode.VALIDATION_ERROR, message);
        }
    }

    private OfficialMediaImportRequest.OfficialMediaItem toOfficialMediaItem(OfficialMediaDraft draft) {
        if (draft == null) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "media item cannot be null");
        }
        OfficialMediaImportRequest.OfficialMediaItem item = new OfficialMediaImportRequest.OfficialMediaItem();
        item.setPoiId(draft.poiId());
        item.setImagePath(draft.imagePath());
        item.setYear(draft.year());
        item.setDescription(draft.description());
        item.setReviewStatus(draft.reviewStatus());
        return item;
    }

    public record OfficialMediaDraft(Long poiId,
                                     String imagePath,
                                     Integer year,
                                     String description,
                                     String reviewStatus) {
    }

    public record OperationResult(String status, String targetType, Long id, String message) {
    }
}
