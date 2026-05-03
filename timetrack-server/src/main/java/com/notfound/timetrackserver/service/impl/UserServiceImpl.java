package com.notfound.timetrackserver.service.impl;

import com.notfound.timetrackcommon.api.ResultCode;
import com.notfound.timetrackcommon.exception.BizException;
import com.notfound.timetrackpojo.dto.UgcUploadRequest;
import com.notfound.timetrackpojo.dto.WechatLoginRequest;
import com.notfound.timetrackpojo.entity.MediaEntity;
import com.notfound.timetrackpojo.entity.PoiEntity;
import com.notfound.timetrackpojo.entity.UserEntity;
import com.notfound.timetrackpojo.vo.MediaVO;
import com.notfound.timetrackpojo.vo.UserLoginVO;
import com.notfound.timetrackpojo.vo.UserProfileVO;
import com.notfound.timetrackserver.mapper.MediaMapper;
import com.notfound.timetrackserver.mapper.PoiMapper;
import com.notfound.timetrackserver.mapper.UserMapper;
import com.notfound.timetrackserver.service.FileStorageService;
import com.notfound.timetrackserver.service.UserService;
import com.notfound.timetrackserver.service.WechatAuthService;
import com.notfound.timetrackserver.security.UserAuthInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserStructMapper userStructMapper;
    private final WechatAuthService wechatAuthService;
    private final UserAuthInterceptor userAuthInterceptor;

    private final FileStorageService fileStorageService;
    private final MediaMapper mediaMapper;
    private final PoiMapper poiMapper;

    public UserServiceImpl(UserMapper userMapper,
                           UserStructMapper userStructMapper,
                           WechatAuthService wechatAuthService,
                           UserAuthInterceptor userAuthInterceptor,
                           FileStorageService fileStorageService,   // 新增
                           MediaMapper mediaMapper,                 // 新增
                           PoiMapper poiMapper) {
        this.userMapper = userMapper;
        this.userStructMapper = userStructMapper;
        this.wechatAuthService = wechatAuthService;
        this.userAuthInterceptor = userAuthInterceptor;
        this.fileStorageService = fileStorageService;
        this.mediaMapper = mediaMapper;
        this.poiMapper = poiMapper;
    }

    @Override
    public UserLoginVO wxLogin(WechatLoginRequest request) {
        String openId = wechatAuthService.code2SessionOpenId(request.getCode());

        UserEntity entity = userMapper.findByOpenId(openId);
        if (entity == null) {
            entity = new UserEntity();
            entity.setOpenId(openId);
            entity.setNickname(defaultIfBlank(request.getNickname(), "TimeTrack User"));
            entity.setAvatarUrl(request.getAvatarUrl());
            entity.setCreateTime(LocalDateTime.now());
            entity.setUpdateTime(LocalDateTime.now());
            userMapper.insert(entity);
        } else {
            entity.setNickname(defaultIfBlank(request.getNickname(), entity.getNickname()));
            entity.setAvatarUrl(defaultIfBlank(request.getAvatarUrl(), entity.getAvatarUrl()));
            entity.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(entity);
        }

        UserProfileVO profile = userStructMapper.toProfileVO(entity);
        String token = userAuthInterceptor.issueToken(entity.getId());

        UserLoginVO vo = new UserLoginVO();
        vo.setToken(token);
        vo.setProfile(profile);
        return vo;
    }

    @Override
    public UserProfileVO getById(Long id) {
        UserEntity entity = userMapper.findById(id);
        if (entity == null) {
            throw new BizException(ResultCode.NOT_FOUND, "user not found: " + id);
        }
        return userStructMapper.toProfileVO(entity);
    }

    private String defaultIfBlank(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }


    @Override
    public MediaVO uploadUgc(UgcUploadRequest request, MultipartFile file, Long userId) {
        // 1. 校验 POI 存在且上架
        PoiEntity poi = poiMapper.findById(request.getPoiId());
        if (poi == null) {
            throw new BizException(ResultCode.NOT_FOUND, "地点不存在");
        }
        if (poi.getStatus() == null || poi.getStatus() != 1) {
            throw new BizException(ResultCode.BIZ_ERROR, "该地点已下架，无法上传影像");
        }

        // 2. 保存图片文件
        String imagePath = fileStorageService.saveUgcImage(userId, file);

        // 3. 构建 MediaEntity
        MediaEntity entity = new MediaEntity();
        entity.setPoiId(request.getPoiId());
        entity.setType("ugc");
        entity.setImagePath(imagePath);
        entity.setYear(request.getYear());
        entity.setDescription(request.getDescription());
        entity.setUploadUserId(userId);
        entity.setReviewStatus("pending");   // 待审核
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        // review_time, reviewer_id 留空

        mediaMapper.insert(entity);

        // 4. 转换为 MediaVO 返回
        MediaVO vo = new MediaVO();
        vo.setId(entity.getId());
        vo.setPoiId(entity.getPoiId());
        vo.setType(entity.getType());
        vo.setImagePath(entity.getImagePath());
        vo.setYear(entity.getYear());
        vo.setDescription(entity.getDescription());
        vo.setUploadUserId(entity.getUploadUserId());
        vo.setReviewStatus(entity.getReviewStatus());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }
}
