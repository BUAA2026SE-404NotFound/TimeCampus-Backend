package com.notfound.timecampusserver.service.impl;

import com.notfound.timecampuspojo.entity.CommentEntity;
import com.notfound.timecampuspojo.entity.UserEntity;
import com.notfound.timecampuspojo.vo.CommentVO;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CommentStructMapper {

    public CommentVO toVO(CommentEntity entity) {
        return toVO(entity, null);
    }

    public CommentVO toVO(CommentEntity entity, Map<Long, UserEntity> users) {
        if (entity == null) {
            return null;
        }
        CommentVO vo = new CommentVO();
        vo.setId(entity.getId());
        vo.setUserId(entity.getUserId());
        vo.setTargetType(entity.getTargetType());
        vo.setTargetId(entity.getTargetId());
        vo.setContent(entity.getContent());
        vo.setReviewStatus(entity.getReviewStatus());
        vo.setRejectReason(entity.getRejectReason());
        vo.setReviewTime(entity.getReviewTime());
        vo.setReviewerId(entity.getReviewerId());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        if (users != null) {
            UserEntity user = users.get(entity.getUserId());
            if (user != null) {
                vo.setNickname(user.getNickname());
            }
        }
        return vo;
    }
}
