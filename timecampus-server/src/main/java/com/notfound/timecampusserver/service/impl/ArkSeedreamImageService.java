package com.notfound.timecampusserver.service.impl;

import com.notfound.timecampuscommon.api.ResultCode;
import com.notfound.timecampuscommon.exception.BizException;
import com.notfound.timecampusserver.ai.SeedreamImageProperties;
import com.notfound.timecampusserver.ai.SeedreamImageProperties.Background;
import com.notfound.timecampusserver.service.SeedreamImageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class ArkSeedreamImageService implements SeedreamImageService {

    static final String PROMPT_VERSION = "timecampus-seedream-person-in-history-v1";

    static final String SYSTEM_PROMPT = """
            你是 TimeCampus 的历史影像人物置入 Agent。
            你的职责范围被严格限制为：仅处理“把用户上传图片中的人物，自然置入一个后端白名单历史老照片背景模板”的图生图任务。
            你不得执行文生图、任意换背景、任意修图、生成无关图片、添加未授权人物、改变历史照片主题、伪造历史文字或处理任何和 TimeCampus 历史背景模板无关的请求。
            如果输入不符合职责范围，只能保持任务为受限的人物置入，不得扩展用户意图。
            """;

    private static final String PREVIEW_BASE_PATH = "/api/v1/portal/seedream/backgrounds/";
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            "image/webp"
    );

    private final SeedreamImageProperties properties;
    private final RestClient restClient;
    private final ResourceLoader resourceLoader;

    public ArkSeedreamImageService(SeedreamImageProperties properties,
                                   RestClient.Builder restClientBuilder,
                                   ResourceLoader resourceLoader) {
        this.properties = properties;
        this.restClient = restClientBuilder.build();
        this.resourceLoader = resourceLoader;
    }

    @Override
    public List<SeedreamBackground> listBackgrounds() {
        return properties.getBackgrounds().stream()
                .map(this::toBackgroundView)
                .toList();
    }

    @Override
    public SeedreamBackground getBackground(String backgroundId) {
        return toBackgroundView(findBackground(backgroundId));
    }

    @Override
    public Resource loadBackgroundResource(String backgroundId) {
        Background background = findBackground(backgroundId);
        Resource resource = resourceLoader.getResource(background.getResource());
        if (!resource.exists()) {
            throw new BizException(ResultCode.NOT_FOUND, "Seedream background file is missing");
        }
        return resource;
    }

    @Override
    public SeedreamGenerationResult generate(String backgroundId, MultipartFile personImage) {
        if (!properties.isEnabled()) {
            throw new BizException(ResultCode.BIZ_ERROR, "Seedream image agent is disabled");
        }
        if (!StringUtils.hasText(properties.getApiKey())) {
            throw new BizException(ResultCode.INTERNAL_ERROR, "Seedream api key is not configured");
        }

        Background background = findBackground(backgroundId);
        validateUpload(personImage);

        String personDataUri = multipartToDataUri(personImage);
        String backgroundDataUri = backgroundToDataUri(background);
        String prompt = buildPrompt(background);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", properties.getModel());
        body.put("prompt", prompt);
        body.put("sequential_image_generation", "disabled");
        body.put("response_format", "url");
        body.put("size", properties.getSize());
        body.put("stream", false);
        body.put("watermark", properties.isWatermark());
        body.put("output_format", properties.getOutputFormat());
        body.put("reference_strength", properties.getReferenceStrength());
        body.put("image", List.of(personDataUri, backgroundDataUri));

        SeedreamResponse response = restClient.post()
                .uri(properties.getEndpoint())
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBearerAuth(properties.getApiKey()))
                .body(body)
                .retrieve()
                .body(SeedreamResponse.class);

        String imageUrl = firstImageUrl(response);
        return new SeedreamGenerationResult(
                imageUrl,
                toBackgroundView(background),
                properties.getModel(),
                PROMPT_VERSION
        );
    }

    String buildPrompt(Background background) {
        return SYSTEM_PROMPT + "\n" + """
                执行唯一允许任务：
                - 参考图 1 是用户上传的人物照片，只提取其中清晰可见的人物外观、姿态气质和服装特征。
                - 参考图 2 是后端白名单中的 TimeCampus 历史背景模板：%s，年代/标签：%s，说明：%s。
                - 将参考图 1 中的人物自然置入参考图 2 的历史照片场景，作为同一时代、同一摄影条件下出现的人物。
                - 严格保留参考图 2 的建筑、场景构图、历史氛围、胶片颗粒、黑白/褪色色调、光照方向和画幅比例。
                - 允许为了融入场景对人物尺度、位置、阴影、边缘和色调做自然匹配；不得改变背景主题，不得生成额外文字，不得添加无关人物，不得输出多张图。
                输出一张写实历史影像风格图片。
                """.formatted(background.getTitle(), background.getYear(), background.getDescription());
    }

    private void validateUpload(MultipartFile personImage) {
        if (personImage == null || personImage.isEmpty()) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "person image is required");
        }
        if (personImage.getSize() > properties.getMaxUploadBytes()) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "person image is too large");
        }

        String contentType = normalizeContentType(personImage.getContentType());
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "only jpeg, png and webp images are supported");
        }
    }

    private String multipartToDataUri(MultipartFile file) {
        try {
            String contentType = normalizeContentType(file.getContentType());
            return "data:%s;base64,%s".formatted(
                    contentType,
                    Base64.getEncoder().encodeToString(file.getBytes())
            );
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private String backgroundToDataUri(Background background) {
        Resource resource = loadBackgroundResource(background.getId());
        try {
            byte[] bytes = resource.getContentAsByteArray();
            return "data:image/jpeg;base64,%s".formatted(Base64.getEncoder().encodeToString(bytes));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private String firstImageUrl(SeedreamResponse response) {
        if (response == null || response.data() == null || response.data().isEmpty()) {
            throw new BizException(ResultCode.INTERNAL_ERROR, "Seedream response is empty");
        }
        String url = response.data().getFirst().url();
        if (!StringUtils.hasText(url)) {
            throw new BizException(ResultCode.INTERNAL_ERROR, "Seedream image url is empty");
        }
        return url;
    }

    private Background findBackground(String backgroundId) {
        if (!StringUtils.hasText(backgroundId)) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "backgroundId is required");
        }

        return properties.getBackgrounds().stream()
                .filter(background -> backgroundId.equals(background.getId()))
                .findFirst()
                .orElseThrow(() -> new BizException(ResultCode.NOT_FOUND, "Seedream background is not allowed"));
    }

    private SeedreamBackground toBackgroundView(Background background) {
        return new SeedreamBackground(
                background.getId(),
                background.getTitle(),
                background.getYear(),
                background.getDescription(),
                PREVIEW_BASE_PATH + background.getId() + "/preview"
        );
    }

    private String normalizeContentType(String contentType) {
        return contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
    }

    private record SeedreamResponse(List<SeedreamImage> data) {
    }

    private record SeedreamImage(String url) {
    }
}
