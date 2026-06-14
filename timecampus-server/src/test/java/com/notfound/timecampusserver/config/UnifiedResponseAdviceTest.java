package com.notfound.timecampusserver.config;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UnifiedResponseAdviceTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new PlainController())
            .setControllerAdvice(new UnifiedResponseAdvice())
            .build();
    }

    @Test
    void wrapsTestControllerReturnValue() throws Exception {
        mockMvc.perform(get("/test"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.message").value("ok"))
            .andExpect(jsonPath("$.data.status").value("UP"));
    }

    @Tag(name = "TestUnifiedResponseAdvice", description = "测试响应增强器")
    @RestController
    static class PlainController {
        @GetMapping("/test")
        @Operation(summary = "测试响应处理器", description = "返回一个简单的 Map，测试 UnifiedResponseAdvice 是否正确包装响应。")
        Map<String, Object> test() {
            return Map.of("status", "UP");
        }
    }
}
