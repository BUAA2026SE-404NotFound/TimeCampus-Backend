package com.notfound.timetrackserver.service.impl;

import com.notfound.timetrackcommon.api.ResultCode;
import com.notfound.timetrackcommon.exception.BizException;
import com.notfound.timetrackpojo.dto.AdminLoginRequest;
import com.notfound.timetrackpojo.entity.AdminEntity;
import com.notfound.timetrackpojo.vo.AdminLoginVO;
import com.notfound.timetrackserver.mapper.AdminMapper;
import com.notfound.timetrackserver.security.AdminAuthInterceptor;
import com.notfound.timetrackserver.service.AdminAuthService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthServiceImpl implements AdminAuthService {

    private final AdminMapper adminMapper;
    private final AdminAuthInterceptor adminAuthInterceptor;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminAuthServiceImpl(AdminMapper adminMapper, AdminAuthInterceptor adminAuthInterceptor) {
        this.adminMapper = adminMapper;
        this.adminAuthInterceptor = adminAuthInterceptor;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public AdminLoginVO login(AdminLoginRequest request) {
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

