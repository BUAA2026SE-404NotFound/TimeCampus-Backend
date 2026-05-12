package com.notfound.timetrackserver.controller;

import com.notfound.timetrackcommon.api.ResultCode;
import com.notfound.timetrackpojo.vo.UserLoginVO;
import com.notfound.timetrackpojo.vo.UserProfileVO;
import com.notfound.timetrackserver.controller.user.AuthController;
import com.notfound.timetrackserver.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerWebMvcTest {

    private MockMvc mockMvc;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(userService))
                .setControllerAdvice(new com.notfound.timetrackcommon.exception.GlobalExceptionHandler())
                .build();
    }

    @Test
    void wxLoginReturnsSuccess() throws Exception {
        UserProfileVO profile = new UserProfileVO();
        profile.setId(1L);
        profile.setNickname("nick");
        profile.setAvatarUrl("avatar");

        UserLoginVO loginVO = new UserLoginVO();
        loginVO.setToken("t1");
        loginVO.setProfile(profile);
        when(userService.wxLogin(any())).thenReturn(loginVO);

        mockMvc.perform(post("/api/v1/auth/wechat/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"abc\",\"nickname\":\"nick\",\"avatarUrl\":\"avatar\",\"identityType\":\"STUDENT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.token").value("t1"))
                .andExpect(jsonPath("$.data.profile.id").value(1))
                .andExpect(jsonPath("$.data.profile.nickname").value("nick"));
    }

    @Test
    void wxLoginReturnsValidationErrorWhenCodeBlank() throws Exception {
        mockMvc.perform(post("/api/v1/auth/wechat/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"\",\"identityType\":\"STUDENT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.VALIDATION_ERROR.getCode()))
                .andExpect(jsonPath("$.message", containsString("code")));
    }
}
