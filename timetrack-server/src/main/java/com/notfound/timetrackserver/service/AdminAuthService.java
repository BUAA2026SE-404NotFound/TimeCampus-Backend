package com.notfound.timetrackserver.service;

import com.notfound.timetrackpojo.dto.AdminLoginRequest;
import com.notfound.timetrackpojo.vo.AdminLoginVO;

public interface AdminAuthService {

    AdminLoginVO login(AdminLoginRequest request);

    void logout(String token);
}

