package com.notfound.timecampusserver.service.impl;

import com.notfound.timecampuscommon.api.ResultCode;
import com.notfound.timecampuscommon.exception.BizException;
import com.notfound.timecampuspojo.constant.AdminRoles;
import com.notfound.timecampuspojo.entity.AdminEntity;
import com.notfound.timecampuspojo.vo.AdminAccountVO;
import com.notfound.timecampusserver.mapper.AdminMapper;
import com.notfound.timecampusserver.service.AdminAccountService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class AdminAccountServiceImpl implements AdminAccountService {

    private static final Set<String> ASSIGNABLE_ROLES = Set.of(
            AdminRoles.ADMIN,
            AdminRoles.READ,
            AdminRoles.NONE
    );

    private final AdminMapper adminMapper;

    public AdminAccountServiceImpl(AdminMapper adminMapper) {
        this.adminMapper = adminMapper;
    }

    @Override
    public List<AdminAccountVO> listAccounts(Long operatorId) {
        requireSuperAdmin(operatorId);
        return adminMapper.findAll().stream().map(this::toVO).toList();
    }

    @Override
    public AdminAccountVO updateStatus(Long adminId, boolean enabled, Long operatorId) {
        requireSuperAdmin(operatorId);
        AdminEntity target = requireAdmin(adminId);
        if (isSuperAdmin(target)) {
            throw new BizException(ResultCode.FORBIDDEN, "cannot change super admin status");
        }
        if (adminId != null && adminId.equals(operatorId) && !enabled) {
            throw new BizException(ResultCode.FORBIDDEN, "cannot disable yourself");
        }

        target.setStatus(enabled ? 1 : 0);
        adminMapper.updateStatus(target);
        return toVO(target);
    }

    @Override
    public AdminAccountVO updateRole(Long adminId, String role, Long operatorId) {
        requireSuperAdmin(operatorId);
        requireAssignableRole(role);
        AdminEntity target = requireAdmin(adminId);
        if (adminId != null && adminId.equals(operatorId)) {
            throw new BizException(ResultCode.FORBIDDEN, "cannot change your own role");
        }

        if (isSuperAdmin(target)) {
            throw new BizException(ResultCode.FORBIDDEN, "cannot change super admin role");
        }
        target.setRole(role);
        adminMapper.updateRole(target);
        return toVO(target);
    }

    private void requireSuperAdmin(Long operatorId) {
        if (operatorId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "missing admin token");
        }
        AdminEntity operator = adminMapper.findById(operatorId);
        if (!isSuperAdmin(operator)) {
            throw new BizException(ResultCode.FORBIDDEN, "super admin required");
        }
    }

    private AdminEntity requireAdmin(Long adminId) {
        AdminEntity admin = adminMapper.findById(adminId);
        if (admin == null) {
            throw new BizException(ResultCode.NOT_FOUND, "admin not found");
        }
        return admin;
    }

    private boolean isSuperAdmin(AdminEntity admin) {
        if (admin == null) {
            return false;
        }
        String role = admin.getRole();
        return AdminRoles.SUPER.equalsIgnoreCase(role);
    }

    private void requireAssignableRole(String role) {
        if (!ASSIGNABLE_ROLES.contains(role)) {
            throw new BizException(ResultCode.BIZ_ERROR, "invalid admin role");
        }
    }

    private AdminAccountVO toVO(AdminEntity admin) {
        AdminAccountVO vo = new AdminAccountVO();
        vo.setId(admin.getId());
        vo.setAdminName(admin.getAdminName());
        vo.setRole(admin.getRole());
        vo.setStatus(admin.getStatus());
        vo.setCreateTime(admin.getCreateTime());
        return vo;
    }
}
