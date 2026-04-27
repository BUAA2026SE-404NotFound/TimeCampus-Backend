-- 用户表
CREATE TABLE `user`
(
    `id`          bigint      NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `openid`      varchar(64) NOT NULL COMMENT '微信 openid',
    `nickname`    varchar(64)          DEFAULT '' COMMENT '用户昵称',
    `avatar_url`  varchar(512)         DEFAULT '' COMMENT '头像 URL',
    `identity`    varchar(20)          DEFAULT NULL COMMENT '身份：current / graduate',
    `enroll_year` int                  DEFAULT NULL COMMENT '入学年份',
    `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（自动更新）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_openid` (`openid`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';

-- 地点表
CREATE TABLE `poi`
(
    `id`          bigint         NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `name`        varchar(128)   NOT NULL COMMENT '地点名称（如 主楼）',
    `latitude`    decimal(10, 8) NOT NULL COMMENT '纬度（精确到厘米级）',
    `longitude`   decimal(11, 8) NOT NULL COMMENT '经度',
    `description` text                    DEFAULT NULL COMMENT '地点简介',
    `fun_fact`    text                    DEFAULT NULL COMMENT '冷知识 / 小故事（可选）',
    `status`      tinyint                 DEFAULT '1' COMMENT '1: 上架, 0: 下架',
    `create_time` datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（自动更新）',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='地点表';

-- 媒体表
CREATE TABLE `media`
(
    `id`             bigint       NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `poi_id`         bigint       NOT NULL COMMENT '关联地点 ID',
    `type`           varchar(20)  NOT NULL COMMENT '类型：official / ugc',
    `image_path`     varchar(512) NOT NULL COMMENT '图片文件路径（挂载目录下的相对路径或绝对路径）',
    `year`           int          NOT NULL COMMENT '拍摄年份',
    `description`    varchar(512)          DEFAULT NULL COMMENT '影像说明',
    `upload_user_id` bigint                DEFAULT NULL COMMENT 'UGC 上传者 ID（官方为 NULL）',
    `review_status`  varchar(20)  NOT NULL DEFAULT 'pending' COMMENT 'pending / approved / rejected',
    `reject_reason`  varchar(255)          DEFAULT NULL COMMENT '驳回原因',
    `review_time`    datetime              DEFAULT NULL COMMENT '审核时间',
    `reviewer_id`    bigint                DEFAULT NULL COMMENT '审核人 ID（关联 user.id）',
    `create_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（自动更新）',
    PRIMARY KEY (`id`),
    KEY `idx_poi_id` (`poi_id`),
    KEY `idx_review_status` (`review_status`),
    KEY `idx_year` (`year`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='媒体表';

-- 收藏表
CREATE TABLE `favorite`
(
    `id`          bigint      NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `user_id`     bigint      NOT NULL COMMENT '用户 ID',
    `target_type` varchar(20) NOT NULL COMMENT '目标类型：poi / media',
    `target_id`   bigint      NOT NULL COMMENT '目标 ID（地点 ID 或影像 ID）',
    `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='收藏表';

-- 管理员账户表
CREATE TABLE `admin`
(
    `id`              bigint       NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `user_id`         bigint                DEFAULT NULL COMMENT '关联用户 ID（可选）',
    `username`        varchar(64)  NOT NULL COMMENT '管理员登录名（唯一）',
    `password_hash`   varchar(255) NOT NULL COMMENT '密码哈希',
    `status`          tinyint      NOT NULL DEFAULT '1' COMMENT '状态：1 启用，0 禁用',
    `last_login_time` datetime              DEFAULT NULL COMMENT '最后登录时间',
    `create_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（自动更新）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_admin_username` (`username`),
    UNIQUE KEY `uk_admin_user_id` (`user_id`),
    KEY `idx_admin_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='管理员账户表';

-- 评论表（可评论 poi 或 media）
CREATE TABLE `comment`
(
    `id`            bigint        NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `user_id`       bigint        NOT NULL COMMENT '评论用户 ID',
    `target_type`   varchar(20)   NOT NULL COMMENT '评论目标类型：poi / media',
    `target_id`     bigint        NOT NULL COMMENT '评论目标 ID（关联 poi.id 或 media.id）',
    `content`       varchar(1000) NOT NULL COMMENT '评论内容',
    `review_status` varchar(20)   NOT NULL DEFAULT 'pending' COMMENT '审核状态：pending / approved / rejected',
    `reject_reason` varchar(255)           DEFAULT NULL COMMENT '驳回原因',
    `review_time`   datetime               DEFAULT NULL COMMENT '审核时间',
    `reviewer_id`   bigint                 DEFAULT NULL COMMENT '审核人 ID（关联 admin_user.id）',
    `create_time`   datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（自动更新）',
    PRIMARY KEY (`id`),
    KEY `idx_comment_user_id` (`user_id`),
    KEY `idx_comment_target` (`target_type`, `target_id`),
    KEY `idx_comment_review_status` (`review_status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='评论表';

-- 日志表（仅管理员可查看）
CREATE TABLE `log`
(
    `id`            bigint      NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `operator_type` varchar(20) NOT NULL COMMENT '操作人类型：admin / user / system',
    `operator_id`   bigint               DEFAULT NULL COMMENT '操作人 ID（admin_user.id 或 user.id）',
    `type`          varchar(32) NOT NULL COMMENT '日志分类：auth / content / review / behavior / system',
    `action`        varchar(64) NOT NULL COMMENT '操作动作',
    `target_type`   varchar(32)          DEFAULT NULL COMMENT '操作对象类型',
    `target_id`     bigint               DEFAULT NULL COMMENT '操作对象 ID',
    `detail`        text                 DEFAULT NULL COMMENT '详细信息（JSON）',
    `create_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_log_operator` (`operator_type`, `operator_id`),
    KEY `idx_log_type` (`type`),
    KEY `idx_log_target` (`target_type`, `target_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='日志表';

