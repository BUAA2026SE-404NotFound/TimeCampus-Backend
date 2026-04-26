-- 用户表
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `openid` varchar(64) NOT NULL COMMENT '微信 openid（唯一）',
  `nickname` varchar(64) DEFAULT '' COMMENT '用户昵称',
  `avatar_url` varchar(512) DEFAULT '' COMMENT '头像 URL',
  `role` varchar(20) DEFAULT 'normal' COMMENT '角色：normal / admin',
  `identity` varchar(20) DEFAULT NULL COMMENT '身份：current / graduate',
  `enroll_year` int DEFAULT NULL COMMENT '入学年份',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（自动更新）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 地点表
CREATE TABLE `poi` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `name` varchar(128) NOT NULL COMMENT '地点名称（如 主楼）',
  `latitude` decimal(10,8) NOT NULL COMMENT '纬度（精确到厘米级）',
  `longitude` decimal(11,8) NOT NULL COMMENT '经度',
  `description` text DEFAULT NULL COMMENT '地点简介',
  `fun_fact` text DEFAULT NULL COMMENT '冷知识 / 小故事（可选）',
  `status` tinyint DEFAULT '1' COMMENT '1: 上架, 0: 下架',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（自动更新）',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地点表';

-- 历史影像表
CREATE TABLE `historical_media` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `poi_id` bigint NOT NULL COMMENT '关联地点 ID',
  `type` varchar(20) NOT NULL COMMENT '类型：official / ugc',
  `image_url` varchar(512) NOT NULL COMMENT '图片链接（腾讯云 COS）',
  `year` int NOT NULL COMMENT '拍摄年份',
  `description` varchar(512) DEFAULT NULL COMMENT '影像说明',
  `upload_user_id` bigint DEFAULT NULL COMMENT 'UGC 上传者 ID（官方为 NULL）',
  `review_status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT 'pending / approved / rejected',
  `reject_reason` varchar(255) DEFAULT NULL COMMENT '驳回原因',
  `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  `reviewer_id` bigint DEFAULT NULL COMMENT '审核人 ID（关联 user.id）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（自动更新）',
  PRIMARY KEY (`id`),
  KEY `idx_poi_id` (`poi_id`),
  KEY `idx_review_status` (`review_status`),
  KEY `idx_year` (`year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='历史影像表';

-- 收藏表
CREATE TABLE `favorite` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `target_type` varchar(20) NOT NULL COMMENT '目标类型：poi / media',
  `target_id` bigint NOT NULL COMMENT '目标 ID（地点 ID 或影像 ID）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- 管理端操作日志表
CREATE TABLE `admin_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `admin_id` bigint NOT NULL COMMENT '管理员 ID',
  `action` varchar(64) NOT NULL COMMENT '操作动作',
  `target_type` varchar(32) DEFAULT NULL COMMENT '操作对象类型',
  `target_id` bigint DEFAULT NULL COMMENT '操作对象 ID',
  `detail` text DEFAULT NULL COMMENT '详细信息（JSON）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理端操作日志表';