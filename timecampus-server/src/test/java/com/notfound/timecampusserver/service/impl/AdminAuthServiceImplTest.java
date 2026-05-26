package com.notfound.timecampusserver.service.impl;

import com.notfound.timecampuscommon.api.ResultCode;
import com.notfound.timecampuscommon.exception.BizException;
import com.notfound.timecampuspojo.dto.AdminLoginRequest;
import com.notfound.timecampuspojo.entity.AdminEntity;
import com.notfound.timecampuspojo.vo.AdminLoginVO;
import com.notfound.timecampusserver.mapper.AdminMapper;
import com.notfound.timecampusserver.security.AdminAuthInterceptor;
import com.notfound.timecampusserver.service.CaptchaVerificationService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AdminAuthServiceImplTest {

    @Test
    void loginVerifiesCaptchaBeforeCredentialLookup() {
        AdminMapper adminMapper = mock(AdminMapper.class);
        AdminAuthInterceptor adminAuthInterceptor = mock(AdminAuthInterceptor.class);
        CaptchaVerificationService captchaVerificationService = mock(CaptchaVerificationService.class);
        AdminAuthServiceImpl service = new AdminAuthServiceImpl(adminMapper, adminAuthInterceptor, captchaVerificationService);
        AdminLoginRequest request = loginRequest("captcha-token");
        doThrow(new BizException(ResultCode.UNAUTHORIZED, "captcha verification failed"))
                .when(captchaVerificationService)
                .verifyLoginToken("captcha-token");

        assertThatThrownBy(() -> service.login(request))
                .isInstanceOf(BizException.class)
                .hasMessage("captcha verification failed");
        verify(captchaVerificationService).verifyLoginToken("captcha-token");
        verifyNoInteractions(adminMapper, adminAuthInterceptor);
    }

    @Test
    void loginIssuesTokenAfterCaptchaAndPasswordPass() {
        AdminMapper adminMapper = mock(AdminMapper.class);
        AdminAuthInterceptor adminAuthInterceptor = mock(AdminAuthInterceptor.class);
        CaptchaVerificationService captchaVerificationService = mock(CaptchaVerificationService.class);
        AdminAuthServiceImpl service = new AdminAuthServiceImpl(adminMapper, adminAuthInterceptor, captchaVerificationService);
        AdminLoginRequest request = loginRequest("captcha-token");
        AdminEntity admin = new AdminEntity();
        admin.setId(7L);
        admin.setAdminName("admin");
        admin.setPassword("123456");
        admin.setStatus(1);
        when(adminMapper.findByAdminName("admin")).thenReturn(admin);
        when(adminAuthInterceptor.issueToken(7L)).thenReturn("admin-token");

        AdminLoginVO result = service.login(request);

        assertThat(result.getToken()).isEqualTo("admin-token");
        assertThat(result.getAdminId()).isEqualTo(7L);
        verify(captchaVerificationService).verifyLoginToken("captcha-token");
        verify(adminMapper).updateLastLoginTime(7L);
    }

    private AdminLoginRequest loginRequest(String capToken) {
        AdminLoginRequest request = new AdminLoginRequest();
        request.setAdminName("admin");
        request.setPassword("123456");
        request.setCapToken(capToken);
        return request;
    }
}
