package com.notfound.timetrackserver.controller;

import com.notfound.timetrackpojo.vo.CommentVO;
import com.notfound.timetrackserver.controller.admin.AdminCommentController;
import com.notfound.timetrackserver.security.AdminContext;
import com.notfound.timetrackserver.service.CommentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminCommentControllerWebMvcTest {

    @AfterEach
    void tearDown() {
        AdminContext.clear();
    }

    @Test
    void listCommentsUsesAdminApi() throws Exception {
        CommentService commentService = mock(CommentService.class);
        CommentVO vo = new CommentVO();
        vo.setId(2L);
        vo.setReviewStatus("pending");
        when(commentService.listForAdmin(null, null, "pending")).thenReturn(List.of(vo));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AdminCommentController(commentService)).build();

        mockMvc.perform(get("/api/v1/admin/comments").param("status", "pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].id").value(2));
    }

    @Test
    void approveCommentUsesAdminContext() throws Exception {
        AdminContext.setAdminId(99L);
        CommentService commentService = mock(CommentService.class);
        CommentVO vo = new CommentVO();
        vo.setId(2L);
        vo.setReviewStatus("approved");
        when(commentService.approve(2L, 99L)).thenReturn(vo);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AdminCommentController(commentService)).build();

        mockMvc.perform(post("/api/v1/admin/comments/2/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.reviewStatus").value("approved"));
    }
}
