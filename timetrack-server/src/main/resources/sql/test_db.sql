-- 初始化测试数据库：test_db
-- 用于本地/CI 环境的数据库连接测试

CREATE DATABASE IF NOT EXISTS `test_db`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `test_db`;

CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `open_id` VARCHAR(128) NOT NULL COMMENT '微信 openid',
  `nickname` VARCHAR(128) NOT NULL COMMENT '昵称',
  `avatar_url` VARCHAR(512) DEFAULT NULL COMMENT '头像地址',
  `created_at` DATETIME NOT NULL COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_open_id` (`open_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='微信小程序用户表';

INSERT INTO `user` (`open_id`, `nickname`, `avatar_url`, `created_at`, `updated_at`)
VALUES ('test_open_id_001', '测试用户', NULL, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  `nickname` = VALUES(`nickname`),
  `updated_at` = VALUES(`updated_at`);

