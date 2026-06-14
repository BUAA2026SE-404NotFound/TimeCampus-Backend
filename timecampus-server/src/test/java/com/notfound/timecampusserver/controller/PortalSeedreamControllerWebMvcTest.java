package com.notfound.timecampusserver.controller;

import com.notfound.timecampusserver.controller.publicapi.PortalSeedreamController;
import com.notfound.timecampusserver.service.SeedreamImageService;
import com.notfound.timecampusserver.service.SeedreamImageService.SeedreamBackground;
import com.notfound.timecampusserver.service.SeedreamImageService.SeedreamGenerationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PortalSeedreamControllerWebMvcTest {

    private MockMvc mockMvc;
    private SeedreamImageService seedreamImageService;

    @BeforeEach
    void setUp() {
        seedreamImageService = mock(SeedreamImageService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PortalSeedreamController(seedreamImageService))
                .build();
    }

    @Test
    void backgroundsReturnsAllowedTemplates() throws Exception {
        when(seedreamImageService.listBackgrounds()).thenReturn(List.of(
                new SeedreamBackground(
                        "campus-gate-001",
                        "北航校门",
                        "历史影像",
                        "北航校门历史场景",
                        "/api/v1/portal/seedream/backgrounds/campus-gate-001/preview"
                )
        ));

        mockMvc.perform(get("/api/v1/portal/seedream/backgrounds"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].id").value("campus-gate-001"))
                .andExpect(jsonPath("$.data[0].previewUrl").value("/api/v1/portal/seedream/backgrounds/campus-gate-001/preview"));
    }

    @Test
    void generateAcceptsOnlyFileAndBackgroundIdFormFields() throws Exception {
        SeedreamBackground background = new SeedreamBackground(
                "campus-gate-001",
                "北航校门",
                "历史影像",
                "北航校门历史场景",
                "/api/v1/portal/seedream/backgrounds/campus-gate-001/preview"
        );
        when(seedreamImageService.generate(eq("campus-gate-001"), any()))
                .thenReturn(new SeedreamGenerationResult(
                        "https://example.com/generated.jpg",
                        background,
                        "doubao-seedream-5.0-lite",
                        "timecampus-seedream-person-in-history-v1"
                ));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "person.png",
                "image/png",
                new byte[]{1, 2, 3}
        );

        mockMvc.perform(multipart("/api/v1/portal/seedream/generations")
                        .file(file)
                        .param("backgroundId", "campus-gate-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.imageUrl").value("https://example.com/generated.jpg"))
                .andExpect(jsonPath("$.data.background.id").value("campus-gate-001"))
                .andExpect(jsonPath("$.data.promptVersion").value("timecampus-seedream-person-in-history-v1"));
    }
}
