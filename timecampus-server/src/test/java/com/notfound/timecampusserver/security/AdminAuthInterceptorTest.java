package com.notfound.timecampusserver.security;

import com.notfound.timecampuscommon.exception.BizException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.notfound.timecampuspojo.entity.AdminEntity;
import com.notfound.timecampusserver.mapper.AdminMapper;

class AdminAuthInterceptorTest {

    @AfterEach
    void tearDown() {
        AdminContext.clear();
    }

    @Test
    void signedAdminMediaFileRequestBypassesBearerToken() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        AdminMapper adminMapper = mock(AdminMapper.class);
        AdminAuthInterceptor interceptor = new AdminAuthInterceptor(redisTemplate, adminMapper);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/admin/media/9/file");
        request.setParameter("accessToken", "signed-admin-media-token");

        assertThat(interceptor.preHandle(request, new MockHttpServletResponse(), new Object())).isTrue();
        assertThat(AdminContext.getAdminId()).isNull();
        verifyNoInteractions(redisTemplate, adminMapper);
    }

    @Test
    void adminMediaFileWithoutAccessTokenStillRequiresBearerToken() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        AdminMapper adminMapper = mock(AdminMapper.class);
        AdminAuthInterceptor interceptor = new AdminAuthInterceptor(redisTemplate, adminMapper);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/admin/media/9/file");

        assertThatThrownBy(() -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("missing admin token");
    }

    @Test
    void accessTokenDoesNotBypassOtherAdminRoutes() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        AdminMapper adminMapper = mock(AdminMapper.class);
        AdminAuthInterceptor interceptor = new AdminAuthInterceptor(redisTemplate, adminMapper);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/admin/map/overview");
        request.setParameter("accessToken", "signed-admin-media-token");

        assertThatThrownBy(() -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("missing admin token");
    }

    @Test
    void bearerTokenStillAuthenticatesAdminRequests() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        AdminMapper adminMapper = mock(AdminMapper.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("admin:token:valid-admin-token")).thenReturn("7");
        AdminEntity admin = new AdminEntity();
        admin.setId(7L);
        admin.setRole("read");
        admin.setStatus(1);
        when(adminMapper.findById(7L)).thenReturn(admin);
        AdminAuthInterceptor interceptor = new AdminAuthInterceptor(redisTemplate, adminMapper);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/admin/map/overview");
        request.addHeader("Authorization", "Bearer valid-admin-token");

        assertThat(interceptor.preHandle(request, new MockHttpServletResponse(), new Object())).isTrue();
        assertThat(AdminContext.getAdminId()).isEqualTo(7L);
        assertThat(AdminContext.getAdminRole()).isEqualTo("read");
    }

    @Test
    void superAdminCanReadDashboardStats() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        AdminMapper adminMapper = mock(AdminMapper.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("admin:token:super-token")).thenReturn("1");
        AdminEntity admin = new AdminEntity();
        admin.setId(1L);
        admin.setRole("super");
        admin.setStatus(1);
        when(adminMapper.findById(1L)).thenReturn(admin);
        AdminAuthInterceptor interceptor = new AdminAuthInterceptor(redisTemplate, adminMapper);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/admin/dashboard/stats");
        request.addHeader("Authorization", "Bearer super-token");

        assertThat(interceptor.preHandle(request, new MockHttpServletResponse(), new Object())).isTrue();
        assertThat(AdminContext.getAdminId()).isEqualTo(1L);
        assertThat(AdminContext.getAdminRole()).isEqualTo("super");
    }

    @Test
    void adminEndpointRejectsReadOnlyRole() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        AdminMapper adminMapper = mock(AdminMapper.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("admin:token:token")).thenReturn("8");
        AdminEntity admin = new AdminEntity();
        admin.setId(8L);
        admin.setRole("read");
        admin.setStatus(1);
        when(adminMapper.findById(8L)).thenReturn(admin);
        AdminAuthInterceptor interceptor = new AdminAuthInterceptor(redisTemplate, adminMapper);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/admin/comments/1/approve");
        request.addHeader("Authorization", "Bearer token");

        assertThatThrownBy(() -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("admin permission required");
    }
}
