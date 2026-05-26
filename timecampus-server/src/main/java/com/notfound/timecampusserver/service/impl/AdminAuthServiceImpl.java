package com.notfound.timecampusserver.service.impl;

import com.notfound.timecampuscommon.api.ResultCode;
import com.notfound.timecampuscommon.exception.BizException;
import com.notfound.timecampuspojo.dto.AdminLoginRequest;
import com.notfound.timecampuspojo.entity.AdminEntity;
import com.notfound.timecampuspojo.vo.AdminLoginVO;
import com.notfound.timecampusserver.mapper.AdminMapper;
import com.notfound.timecampusserver.security.AdminAuthInterceptor;
import com.notfound.timecampusserver.service.AdminAuthService;
import com.notfound.timecampusserver.service.CaptchaVerificationService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthServiceImpl implements AdminAuthService {

    private final AdminMapper adminMapper;
    private final AdminAuthInterceptor adminAuthInterceptor;
    private final CaptchaVerificationService captchaVerificationService;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminAuthServiceImpl(AdminMapper adminMapper,
                                AdminAuthInterceptor adminAuthInterceptor,
                                CaptchaVerificationService captchaVerificationService) {
        this.adminMapper = adminMapper;
        this.adminAuthInterceptor = adminAuthInterceptor;
        this.captchaVerificationService = captchaVerificationService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public AdminLoginVO login(AdminLoginRequest request) {
        captchaVerificationService.verifyLoginToken(request.getCapToken());

        AdminEntity admin = adminMapper.findByAdminName(request.getAdminName());
        if (admin == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "invalid admin credentials");
        }
        if (admin.getStatus() != null && admin.getStatus() == 0) {
            throw new BizException(ResultCode.FORBIDDEN, "admin disabled");
        }
        if (!passwordMatches(request.getPassword(), admin.getPassword())) {
            throw new BizException(ResultCode.UNAUTHORIZED, "invalid admin credentials");
        }

        adminMapper.updateLastLoginTime(admin.getId());
        String token = adminAuthInterceptor.issueToken(admin.getId());

        AdminLoginVO vo = new AdminLoginVO();
        vo.setToken(token);
        vo.setAdminId(admin.getId());
        vo.setAdminName(admin.getAdminName());
        return vo;
    }

    @Override
    public void logout(String token) {
        adminAuthInterceptor.revokeToken(token);
    }

    private boolean passwordMatches(String raw, String stored) {
        if (stored == null) {
            return false;
        }
        // If stored is BCrypt hash, verify; otherwise allow plaintext for Alpha bootstrap.
        if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
            return passwordEncoder.matches(raw, stored);
        }
        return stored.equals(raw);
    }
}
