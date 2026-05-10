package com.notfound.timetrackserver.controller;

import com.notfound.timetrackcommon.exception.BizException;
import com.notfound.timetrackcommon.api.ResultCode;
import com.notfound.timetrackpojo.vo.CommentVO;
import com.notfound.timetrackpojo.vo.MediaVO;
import com.notfound.timetrackpojo.vo.UserProfileVO;
import com.notfound.timetrackpojo.vo.UserReviewResultVO;
import com.notfound.timetrackserver.controller.user.UserController;
import com.notfound.timetrackserver.security.UserContext;
import com.notfound.timetrackserver.service.FavoriteService;
import com.notfound.timetrackserver.service.ReviewResultService;
import com.notfound.timetrackserver.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerWebMvcTest {

    private MockMvc mockMvc;

    private UserService userService;
    private FavoriteService favoriteService;
    private ReviewResultService reviewResultService;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        favoriteService = mock(FavoriteService.class);
        reviewResultService = mock(ReviewResultService.class);
        UserController userController = new UserController(userService, favoriteService, reviewResultService);
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new com.notfound.timetrackcommon.exception.GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
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

    @Test
    void reviewResultsReturnsOwnReviewResults() throws Exception {
        UserContext.setUserId(2L);
        UserReviewResultVO vo = new UserReviewResultVO();
        vo.setUserId(2L);
        vo.setReviewStatus("rejected");
        vo.setUgcCount(1);
        vo.setCommentCount(2);
        when(reviewResultService.listByUser(2L, "rejected")).thenReturn(vo);

        mockMvc.perform(get("/api/v1/users/2/review-results?status=rejected").header("Authorization", "Bearer t"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.userId").value(2))
                .andExpect(jsonPath("$.data.reviewStatus").value("rejected"))
                .andExpect(jsonPath("$.data.ugcCount").value(1))
                .andExpect(jsonPath("$.data.commentCount").value(2));
    }

    @Test
    void reviewResultsRejectsOtherUser() throws Exception {
        UserContext.setUserId(1L);

        mockMvc.perform(get("/api/v1/users/2/review-results").header("Authorization", "Bearer t"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.FORBIDDEN.getCode()))
                .andExpect(jsonPath("$.message").value("forbidden"));
    }

    @Test
    void ugcReviewResultsReturnsOnlyUgcItems() throws Exception {
        UserContext.setUserId(2L);
        MediaVO media = new MediaVO();
        media.setId(8L);
        media.setReviewStatus("pending");
        when(reviewResultService.listUgcByUser(2L, "pending")).thenReturn(List.of(media));

        mockMvc.perform(get("/api/v1/users/2/review-results/ugc?status=pending").header("Authorization", "Bearer t"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].id").value(8))
                .andExpect(jsonPath("$.data[0].reviewStatus").value("pending"));
    }

    @Test
    void commentReviewResultsReturnsOnlyComments() throws Exception {
        UserContext.setUserId(2L);
        CommentVO comment = new CommentVO();
        comment.setId(9L);
        comment.setContent("hello");
        comment.setReviewStatus("rejected");
        when(reviewResultService.listCommentsByUser(2L, "rejected")).thenReturn(List.of(comment));

        mockMvc.perform(get("/api/v1/users/2/review-results/comments?status=rejected").header("Authorization", "Bearer t"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].id").value(9))
                .andExpect(jsonPath("$.data[0].content").value("hello"))
                .andExpect(jsonPath("$.data[0].reviewStatus").value("rejected"));
    }
}
