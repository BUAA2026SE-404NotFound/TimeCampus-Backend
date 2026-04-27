## 数据库设计

> 说明：本文档保留外键设计用于建模说明；开发/测试初始化脚本 [schema.sql](../timetrack-server/src/main/resources/sql/schema.sql) 默认不实际创建外键约束，以降低联调和数据准备成本。。

### 1. 用户表 `user`

| 字段名         | 类型           | 允许 NULL | 默认值               | 说明                    |
|:------------|:-------------|:--------|:------------------|:----------------------|
| id          | bigint       | 否       | AUTO_INCREMENT    | 主键 ID                 |
| openid      | varchar(64)  | 否       | -                 | 微信 openid（唯一）         |
| nickname    | varchar(64)  | 是       | ''                | 用户昵称                  |
| avatar_url  | varchar(512) | 是       | ''                | 头像 URL                |
| identity    | varchar(20)  | 是       | NULL              | 身份：current / graduate |
| enroll_year | int          | 是       | NULL              | 入学年份                  |
| create_time | datetime     | 否       | CURRENT_TIMESTAMP | 创建时间                  |
| update_time | datetime     | 否       | CURRENT_TIMESTAMP | 更新时间（自动更新）            |

- 主键：`id`
- 唯一索引：`uk_openid` (`openid`)

------

### 2. 地点表 `poi`

| 字段名         | 类型            | 允许 NULL | 默认值               | 说明            |
|:------------|:--------------|:--------|:------------------|:--------------|
| id          | bigint        | 否       | AUTO_INCREMENT    | 主键 ID         |
| name        | varchar(128)  | 否       | -                 | 地点名称（如 主楼）    |
| latitude    | decimal(10,8) | 否       | -                 | 纬度            |
| longitude   | decimal(11,8) | 否       | -                 | 经度            |
| description | text          | 是       | NULL              | 地点简介          |
| fun_fact    | text          | 是       | NULL              | 冷知识 / 小故事（可选） |
| status      | tinyint       | 是       | 1                 | 1: 上架, 0: 下架  |
| create_time | datetime      | 否       | CURRENT_TIMESTAMP | 创建时间          |
| update_time | datetime      | 否       | CURRENT_TIMESTAMP | 更新时间（自动更新）    |

- 主键：`id`
- 普通索引：`idx_status` (`status`)

------

### 3. 媒体表 `media`

| 字段名            | 类型           | 允许 NULL | 默认值               | 说明                            |
|:---------------|:-------------|:--------|:------------------|:------------------------------|
| id             | bigint       | 否       | AUTO_INCREMENT    | 主键 ID                         |
| poi_id         | bigint       | 否       | -                 | 关联地点 ID                       |
| type           | varchar(20)  | 否       | -                 | 类型：official / ugc             |
| image_path     | varchar(512) | 否       | -                 | 图片文件路径（挂载目录下的相对路径或绝对路径）       |
| year           | int          | 否       | -                 | 拍摄年份                          |
| description    | varchar(512) | 是       | NULL              | 影像说明                          |
| upload_user_id | bigint       | 是       | NULL              | UGC 上传者 ID（官方为 NULL）          |
| review_status  | varchar(20)  | 否       | 'pending'         | pending / approved / rejected |
| reject_reason  | varchar(255) | 是       | NULL              | 驳回原因                          |
| review_time    | datetime     | 是       | NULL              | 审核时间                          |
| reviewer_id    | bigint       | 是       | NULL              | 审核人 ID（关联 user.id）            |
| create_time    | datetime     | 否       | CURRENT_TIMESTAMP | 创建时间                          |
| update_time    | datetime     | 否       | CURRENT_TIMESTAMP | 更新时间（自动更新）                    |

- 主键：`id`
- 普通索引：`idx_poi_id` (`poi_id`)
- 普通索引：`idx_review_status` (`review_status`)
- 普通索引：`idx_year` (`year`)
- 外键：`fk_media_poi` (`poi_id` -> `poi.id`)，`ON DELETE CASCADE ON UPDATE CASCADE`
- 外键：`fk_media_upload_user` (`upload_user_id` -> `user.id`)，`ON DELETE SET NULL ON UPDATE CASCADE`
- 外键：`fk_media_reviewer` (`reviewer_id` -> `user.id`)，`ON DELETE SET NULL ON UPDATE CASCADE`

------

### 4. 收藏表 `favorite`

| 字段名         | 类型          | 允许 NULL | 默认值               | 说明                  |
|:------------|:------------|:--------|:------------------|:--------------------|
| id          | bigint      | 否       | AUTO_INCREMENT    | 主键 ID               |
| user_id     | bigint      | 否       | -                 | 用户 ID               |
| target_type | varchar(20) | 否       | -                 | 目标类型：poi / media    |
| target_id   | bigint      | 否       | -                 | 目标 ID（地点 ID 或影像 ID） |
| create_time | datetime    | 否       | CURRENT_TIMESTAMP | 收藏时间                |

- 主键：`id`
- 唯一索引：`uk_user_target` (`user_id`, `target_type`, `target_id`)
- 外键：`fk_favorite_user` (`user_id` -> `user.id`)，`ON DELETE CASCADE ON UPDATE CASCADE`

------

### 5. 管理员账户表 `admin_user`

| 字段名             | 类型           | 允许 NULL | 默认值               | 说明                  |
|:----------------|:-------------|:--------|:------------------|:--------------------|
| id              | bigint       | 否       | AUTO_INCREMENT    | 主键 ID               |
| user_id         | bigint       | 是       | NULL              | 关联用户 ID（可选）         |
| username        | varchar(64)  | 否       | -                 | 管理员登录名（唯一）          |
| password_hash   | varchar(255) | 否       | -                 | 密码哈希（BCrypt/Argon2） |
| status          | tinyint      | 否       | 1                 | 状态：1 启用，0 禁用        |
| last_login_time | datetime     | 是       | NULL              | 最后登录时间              |
| create_time     | datetime     | 否       | CURRENT_TIMESTAMP | 创建时间                |
| update_time     | datetime     | 否       | CURRENT_TIMESTAMP | 更新时间（自动更新）          |

- 主键：`id`
- 唯一索引：`uk_admin_username` (`username`)
- 唯一索引：`uk_admin_user_id` (`user_id`)
- 普通索引：`idx_admin_status` (`status`)
- 外键：`fk_admin_user_user` (`user_id` -> `user.id`)，`ON DELETE SET NULL ON UPDATE CASCADE`

------

### 6. 评论表 `comment`

| 字段名           | 类型            | 允许 NULL | 默认值               | 说明                                 |
|:--------------|:--------------|:--------|:------------------|:-----------------------------------|
| id            | bigint        | 否       | AUTO_INCREMENT    | 主键 ID                              |
| user_id       | bigint        | 否       | -                 | 评论用户 ID                            |
| target_type   | varchar(20)   | 否       | -                 | 评论目标类型：poi / media                 |
| target_id     | bigint        | 否       | -                 | 评论目标 ID（关联 poi.id 或 media.id）      |
| content       | varchar(1000) | 否       | -                 | 评论内容                               |
| review_status | varchar(20)   | 否       | 'pending'         | 审核状态：pending / approved / rejected |
| reject_reason | varchar(255)  | 是       | NULL              | 驳回原因                               |
| review_time   | datetime      | 是       | NULL              | 审核时间                               |
| reviewer_id   | bigint        | 是       | NULL              | 审核人 ID（关联 admin_user.id）           |
| create_time   | datetime      | 否       | CURRENT_TIMESTAMP | 创建时间                               |
| update_time   | datetime      | 否       | CURRENT_TIMESTAMP | 更新时间（自动更新）                         |

- 主键：`id`
- 普通索引：`idx_comment_user_id` (`user_id`)
- 普通索引：`idx_comment_target` (`target_type`, `target_id`)
- 普通索引：`idx_comment_review_status` (`review_status`)
- 外键：`fk_comment_user` (`user_id` -> `user.id`)，`ON DELETE CASCADE ON UPDATE CASCADE`
- 外键：`fk_comment_reviewer` (`reviewer_id` -> `admin_user.id`)，`ON DELETE SET NULL ON UPDATE CASCADE`
- 说明：`target_id` 与 `target_type` 形成多态关联，数据库层不加外键，由业务层保证目标存在性

------

### 7. 全量日志表 `log`

| 字段名           | 类型          | 允许 NULL | 默认值               | 说明                                               |
|:--------------|:------------|:--------|:------------------|:-------------------------------------------------|
| id            | bigint      | 否       | AUTO_INCREMENT    | 主键 ID                                            |
| operator_type | varchar(20) | 否       | -                 | 操作人类型：admin / user / system                      |
| operator_id   | bigint      | 是       | NULL              | 操作人 ID（admin_user.id 或 user.id）                  |
| type          | varchar(32) | 否       | -                 | 日志分类：auth / content / review / behavior / system |
| action        | varchar(64) | 否       | -                 | 操作动作                                             |
| target_type   | varchar(32) | 是       | NULL              | 操作对象类型                                           |
| target_id     | bigint      | 是       | NULL              | 操作对象 ID                                          |
| detail        | text        | 是       | NULL              | 详细信息（JSON）                                       |
| create_time   | datetime    | 否       | CURRENT_TIMESTAMP | 操作时间                                             |

- 主键：`id`
- 普通索引：`idx_log_operator` (`operator_type`, `operator_id`)
- 普通索引：`idx_log_type` (`type`)
- 普通索引：`idx_log_target` (`target_type`, `target_id`)
- 说明：`log` 表记录管理员与普通用户操作，读取权限在应用层限制为仅管理员可查看
