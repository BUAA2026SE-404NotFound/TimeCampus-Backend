package com.notfound.timecampusserver.config;

import com.notfound.timecampusserver.controller.UnifiedResponseAdviceTestController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UnifiedResponseAdviceTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UnifiedResponseAdviceTestController())
            .setControllerAdvice(new UnifiedResponseAdvice())
            .build();
    }

    @Test
    void wrapsTestControllerReturnValue() throws Exception {
        mockMvc.perform(get("/test-unified-response"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.message").value("ok"))
            .andExpect(jsonPath("$.data.status").value("UP"));
    }
}
