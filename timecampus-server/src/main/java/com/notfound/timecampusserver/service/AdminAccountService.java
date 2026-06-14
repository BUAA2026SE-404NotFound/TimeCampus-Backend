package com.notfound.timecampusserver.service;

import com.notfound.timecampuspojo.vo.AdminAccountVO;

import java.util.List;

public interface AdminAccountService {

    List<AdminAccountVO> listAccounts(Long operatorId);

    AdminAccountVO updateStatus(Long adminId, boolean enabled, Long operatorId);

    AdminAccountVO updateRole(Long adminId, String role, Long operatorId);
}
