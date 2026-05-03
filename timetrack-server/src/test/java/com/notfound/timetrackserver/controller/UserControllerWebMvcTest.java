package com.notfound.timetrackserver.controller;

import com.notfound.timetrackcommon.exception.BizException;
import com.notfound.timetrackcommon.api.ResultCode;
import com.notfound.timetrackpojo.vo.UserProfileVO;
import com.notfound.timetrackserver.controller.user.UserController;
import com.notfound.timetrackserver.service.FavoriteService;
import com.notfound.timetrackserver.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerWebMvcTest {

    private MockMvc mockMvc;

    private UserService userService;
    private FavoriteService favoriteService;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        favoriteService = mock(FavoriteService.class);
        UserController userController = new UserController(userService, favoriteService);
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new com.notfound.timetrackcommon.exception.GlobalExceptionHandler())
                .build();
    }

    @Test
    void getUserReturnsSuccess() throws Exception {
        UserProfileVO vo = new UserProfileVO();
        vo.setId(2L);
        vo.setNickname("u2");

        when(userService.getById(2L)).thenReturn(vo);

        mockMvc.perform(get("/api/v1/users/2").header("Authorization", "Bearer t"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(2));
    }

    @Test
    void getUserReturnsNotFoundWhenServiceThrowsBizException() throws Exception {
        when(userService.getById(9L)).thenThrow(new BizException(ResultCode.NOT_FOUND, "user not found: 9"));

        mockMvc.perform(get("/api/v1/users/9").header("Authorization", "Bearer t"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value("user not found: 9"));
    }
}
