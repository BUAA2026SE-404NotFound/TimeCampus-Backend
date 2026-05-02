package com.notfound.timetrackserver.service;

import com.notfound.timetrackpojo.dto.UgcUploadRequest;
import com.notfound.timetrackpojo.dto.WechatLoginRequest;
import com.notfound.timetrackpojo.vo.MediaVO;
import com.notfound.timetrackpojo.vo.UserLoginVO;
import com.notfound.timetrackpojo.vo.UserProfileVO;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    UserLoginVO wxLogin(WechatLoginRequest request);

    UserProfileVO getById(Long id);

    MediaVO uploadUgc(UgcUploadRequest request, MultipartFile file, Long userId);
}

