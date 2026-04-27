package com.notfound.timetrackserver.service;

import com.notfound.timetrackpojo.dto.WechatLoginRequest;
import com.notfound.timetrackpojo.vo.UserProfileVO;

public interface UserService {

    UserProfileVO wxLogin(WechatLoginRequest request);

    UserProfileVO getById(Long id);
}

