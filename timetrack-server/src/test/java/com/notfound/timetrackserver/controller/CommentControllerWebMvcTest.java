package com.notfound.timetrackserver.controller;

import com.notfound.timetrackpojo.dto.CommentCreateRequest;
import com.notfound.timetrackpojo.vo.CommentVO;
import com.notfound.timetrackserver.controller.user.CommentController;
import com.notfound.timetrackserver.security.UserContext;
import com.notfound.timetrackserver.service.CommentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CommentControllerWebMvcTest {

    private CommentService commentService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        commentService = mock(CommentService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new CommentController(commentService))
                .setControllerAdvice(new com.notfound.timetrackcommon.exception.GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void createUsesVersionedApiAndUserContext() throws Exception {
        UserContext.setUserId(5L);
        CommentVO vo = new CommentVO();
        vo.setId(11L);
        vo.setTargetType("poi");
        vo.setTargetId(7L);
        vo.setReviewStatus("pending");
        when(commentService.create(any(CommentCreateRequest.class), org.mockito.Mockito.eq(5L))).thenReturn(vo);

        mockMvc.perform(post("/api/v1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"targetType":"poi","targetId":7,"content":"hello"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(11))
                .andExpect(jsonPath("$.data.reviewStatus").value("pending"));
    }

    @Test
    void listByTargetReturnsApprovedComments() throws Exception {
        CommentVO vo = new CommentVO();
        vo.setId(12L);
        vo.setNickname("student");
        vo.setContent("nice");
        when(commentService.listByTarget("poi", 7L)).thenReturn(List.of(vo));

        mockMvc.perform(get("/api/v1/comments/poi/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].nickname").value("student"))
                .andExpect(jsonPath("$.data[0].content").value("nice"));
    }
}
