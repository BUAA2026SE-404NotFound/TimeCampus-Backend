package com.notfound.timecampusserver.service;

import com.notfound.timecampuspojo.vo.AdminAccountVO;

public interface AdminAccountService {

    AdminAccountVO updateStatus(Long adminId, boolean enabled, Long operatorId);

    AdminAccountVO updateRole(Long adminId, String role, Long operatorId);
}
