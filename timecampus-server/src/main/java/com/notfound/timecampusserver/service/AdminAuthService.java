package com.notfound.timecampusserver.service;

import com.notfound.timecampuspojo.dto.AdminLoginRequest;
import com.notfound.timecampuspojo.vo.AdminLoginVO;

public interface AdminAuthService {

    AdminLoginVO login(AdminLoginRequest request);

    void logout(String token);
}

