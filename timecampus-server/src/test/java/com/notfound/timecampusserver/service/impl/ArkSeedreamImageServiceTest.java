package com.notfound.timecampusserver.service.impl;

import com.notfound.timecampuscommon.exception.BizException;
import com.notfound.timecampusserver.ai.SeedreamImageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ArkSeedreamImageServiceTest {

    private SeedreamImageProperties properties;
    private RestClient.Builder restClientBuilder;
    private MockRestServiceServer server;
    private ArkSeedreamImageService service;

    @BeforeEach
    void setUp() {
        properties = new SeedreamImageProperties();
        properties.setEnabled(true);
        properties.setApiKey("ark-test");
        properties.setEndpoint("https://ark.example.test/api/plan/v3/images/generations");

        restClientBuilder = RestClient.builder();
        server = MockRestServiceServer.bindTo(restClientBuilder).build();
        service = new ArkSeedreamImageService(properties, restClientBuilder, new DefaultResourceLoader());
    }

    @Test
    void generateBuildsRestrictedImageToImageRequest() {
        server.expect(requestTo(properties.getEndpoint()))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer ark-test"))
                .andExpect(content().string(allOf(
                        containsString("\"model\":\"doubao-seedream-5.0-lite\""),
                        containsString("\"sequential_image_generation\":\"disabled\""),
                        containsString("\"response_format\":\"url\""),
                        containsString("\"image\":[\"data:image/png;base64,AQID\""),
                        containsString("历史影像人物置入 Agent"),
                        containsString("不得执行文生图"),
                        containsString("不得生成额外文字")
                )))
                .andRespond(withSuccess(
                        "{\"data\":[{\"url\":\"https://example.com/generated.jpg\"}]}",
                        MediaType.APPLICATION_JSON
                ));

        var result = service.generate("campus-gate-001", pngFile());

        assertThat(result.imageUrl()).isEqualTo("https://example.com/generated.jpg");
        assertThat(result.background().id()).isEqualTo("campus-gate-001");
        assertThat(result.promptVersion()).isEqualTo(ArkSeedreamImageService.PROMPT_VERSION);
        server.verify();
    }

    @Test
    void unknownBackgroundIsRejectedBeforeApiCall() {
        assertThatThrownBy(() -> service.generate("not-allowed", pngFile()))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("not allowed");
    }

    @Test
    void defaultBackgroundsAreTheConfiguredCampusTemplates() throws Exception {
        var backgrounds = service.listBackgrounds();

        assertThat(backgrounds)
                .extracting(background -> background.id())
                .containsExactly(
                        "campus-gate-001",
                        "building-one-002",
                        "building-eight-001",
                        "main-building-2010",
                        "library-007"
                );

        for (var background : backgrounds) {
            assertThat(service.loadBackgroundResource(background.id()).getContentAsByteArray())
                    .isNotEmpty();
        }
    }

    @Test
    void unsupportedUploadTypeIsRejectedBeforeApiCall() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "person.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "hello".getBytes()
        );

        assertThatThrownBy(() -> service.generate("campus-gate-001", file))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("jpeg, png and webp");
    }

    @Test
    void promptDocumentsAgentBoundary() {
        String prompt = service.buildPrompt(properties.getBackgrounds().getFirst());

        assertThat(prompt)
                .contains("职责范围被严格限制")
                .contains("后端白名单历史老照片背景模板")
                .contains("不得执行文生图")
                .contains("不得扩展用户意图");
    }

    private MockMultipartFile pngFile() {
        return new MockMultipartFile(
                "file",
                "person.png",
                MediaType.IMAGE_PNG_VALUE,
                new byte[]{1, 2, 3}
        );
    }
}
