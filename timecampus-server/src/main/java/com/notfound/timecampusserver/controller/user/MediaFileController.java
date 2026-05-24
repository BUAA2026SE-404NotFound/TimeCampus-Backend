package com.notfound.timecampusserver.controller.user;

import com.notfound.timecampusserver.service.MediaFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Media-File", description = "影像文件访问")
@RestController
@RequestMapping("/api/v1/media")
public class MediaFileController {

    private final MediaFileService mediaFileService;

    public MediaFileController(MediaFileService mediaFileService) {
        this.mediaFileService = mediaFileService;
    }

    @GetMapping("/{id}/file")
    @Operation(summary = "读取影像文件", description = "用于访问保存在本地文件系统或挂载目录中的 media.image_path。必须使用内容接口返回的短期 accessToken。远程 URL 直接由前端访问。")
    public ResponseEntity<Resource> file(@PathVariable Long id, @RequestParam String accessToken) {
        Resource resource = mediaFileService.loadMediaFile(id, accessToken);
        return ResponseEntity.ok()
                .header("Content-Type", mediaFileService.contentType(resource))
                .body(resource);
    }
}
