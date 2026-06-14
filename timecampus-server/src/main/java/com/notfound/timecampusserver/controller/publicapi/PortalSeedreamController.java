package com.notfound.timecampusserver.controller.publicapi;

import com.notfound.timecampuscommon.api.ApiResponse;
import com.notfound.timecampusserver.service.SeedreamImageService;
import com.notfound.timecampusserver.service.SeedreamImageService.SeedreamBackground;
import com.notfound.timecampusserver.service.SeedreamImageService.SeedreamGenerationResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Tag(name = "Portal-Seedream", description = "项目主页：受限历史影像人物置入")
@RestController
@RequestMapping("/api/v1/portal/seedream")
public class PortalSeedreamController {

    private final SeedreamImageService seedreamImageService;

    public PortalSeedreamController(SeedreamImageService seedreamImageService) {
        this.seedreamImageService = seedreamImageService;
    }

    @GetMapping("/backgrounds")
    @Operation(summary = "历史影像背景模板", description = "返回后端白名单中的 Seedream 背景模板。")
    public ApiResponse<List<SeedreamBackground>> backgrounds() {
        return ApiResponse.success(seedreamImageService.listBackgrounds());
    }

    @GetMapping("/backgrounds/{backgroundId}/preview")
    @Operation(summary = "历史影像背景预览", description = "返回白名单背景模板预览图。")
    public ResponseEntity<Resource> preview(@PathVariable String backgroundId) {
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic())
                .body(seedreamImageService.loadBackgroundResource(backgroundId));
    }

    @PostMapping(path = "/generations", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "生成历史影像人物置入图", description = "只允许上传人物图片并选择白名单背景模板，不接收自由提示词。")
    public ApiResponse<SeedreamGenerationResult> generate(@RequestPart("file") MultipartFile file,
                                                          @RequestParam("backgroundId") String backgroundId) {
        return ApiResponse.success(seedreamImageService.generate(backgroundId, file));
    }
}
