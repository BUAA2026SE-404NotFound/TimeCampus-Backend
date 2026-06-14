package com.notfound.timecampusserver.service;

import com.notfound.timecampuspojo.dto.WechatLoginRequest;
import com.notfound.timecampuspojo.vo.UserLoginVO;
import com.notfound.timecampuspojo.vo.UserProfileVO;

public interface UserService {

    UserLoginVO wxLogin(WechatLoginRequest request);

    UserProfileVO getById(Long id);
}
