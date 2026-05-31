package com.notfound.timecampusserver.service.impl;

import com.notfound.timecampuscommon.exception.BizException;
import com.notfound.timecampuspojo.entity.AdminEntity;
import com.notfound.timecampuspojo.vo.AdminAccountVO;
import com.notfound.timecampusserver.mapper.AdminMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminAccountServiceImplTest {

    @Test
    void updateRoleRequiresSuperAdmin() {
        AdminMapper adminMapper = mock(AdminMapper.class);
        AdminAccountServiceImpl service = new AdminAccountServiceImpl(adminMapper);
        AdminEntity operator = new AdminEntity();
        operator.setId(1L);
        operator.setRole("read");
        when(adminMapper.findById(1L)).thenReturn(operator);

        assertThatThrownBy(() -> service.updateRole(2L, "write", 1L))
                .isInstanceOf(BizException.class)
                .hasMessage("super admin required");
    }

    @Test
    void updateRoleUpdatesTargetRole() {
        AdminMapper adminMapper = mock(AdminMapper.class);
        AdminAccountServiceImpl service = new AdminAccountServiceImpl(adminMapper);
        AdminEntity operator = new AdminEntity();
        operator.setId(1L);
        operator.setRole("super");
        AdminEntity target = new AdminEntity();
        target.setId(2L);
        target.setRole("none");
        when(adminMapper.findById(1L)).thenReturn(operator);
        when(adminMapper.findById(2L)).thenReturn(target);

        AdminAccountVO result = service.updateRole(2L, "write", 1L);

        ArgumentCaptor<AdminEntity> captor = ArgumentCaptor.forClass(AdminEntity.class);
        verify(adminMapper).updateRole(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(2L);
        assertThat(captor.getValue().getRole()).isEqualTo("write");
        assertThat(result.getRole()).isEqualTo("write");
    }

    @Test
    void updateStatusRejectsSuperAdminTarget() {
        AdminMapper adminMapper = mock(AdminMapper.class);
        AdminAccountServiceImpl service = new AdminAccountServiceImpl(adminMapper);
        AdminEntity operator = new AdminEntity();
        operator.setId(1L);
        operator.setRole("super");
        AdminEntity target = new AdminEntity();
        target.setId(2L);
        target.setRole("super");
        when(adminMapper.findById(1L)).thenReturn(operator);
        when(adminMapper.findById(2L)).thenReturn(target);

        assertThatThrownBy(() -> service.updateStatus(2L, false, 1L))
                .isInstanceOf(BizException.class)
                .hasMessage("cannot change super admin status");
    }
}
