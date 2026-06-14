package com.notfound.timecampusserver.service;

import com.notfound.timecampuspojo.dto.AdminLoginRequest;
import com.notfound.timecampuspojo.dto.AdminRegisterRequest;
import com.notfound.timecampuspojo.vo.AdminLoginVO;

public interface AdminAuthService {

    AdminLoginVO login(AdminLoginRequest request);

    AdminLoginVO register(AdminRegisterRequest request);

    void logout(String token);
}
