package com.notfound.timetrackserver.service;

import com.notfound.timetrackpojo.dto.WechatLoginRequest;
import com.notfound.timetrackpojo.vo.UserLoginVO;
import com.notfound.timetrackpojo.vo.UserProfileVO;

public interface UserService {

    UserLoginVO wxLogin(WechatLoginRequest request);

    UserProfileVO getById(Long id);
}

