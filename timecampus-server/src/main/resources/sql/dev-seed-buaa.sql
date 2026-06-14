USE `timecampus`;

-- Development seed data around BUAA center.
-- Source center is Tencent GCJ02 longitude 116.34067928, latitude 39.97611493.
-- The database stores GCJ02 so operational maps and Tencent WebService requests share the same coordinate system.
-- This script is idempotent for the 900x seed IDs. Existing POI rows are preserved so local coordinate edits are not overwritten.

DELETE
FROM `favorite`
WHERE `user_id` IN (9001, 9002, 9003);
DELETE
FROM `comment`
WHERE `user_id` IN (9001, 9002, 9003);
DELETE
FROM `media`
WHERE `id` BETWEEN 9001 AND 9010;

INSERT IGNORE INTO `user` (`id`, `openid`, `nickname`, `avatar_url`, `identity`, `enroll_year`, `create_time`,
                           `update_time`)
VALUES (9001, 'dev-buaa-openid-9001', '北航新生小航', '', 'FRESHMAN', 2025, NOW(), NOW()),
       (9002, 'dev-buaa-openid-9002', '学院路老同学', '', 'STUDENT', 2022, NOW(), NOW()),
       (9003, 'dev-buaa-openid-9003', '返校校友', '', 'ALUMNI', 2014, NOW(), NOW());

INSERT IGNORE INTO `poi` (`id`, `name`, `latitude`, `longitude`, `description`, `fun_fact`, `status`, `create_time`,
                          `update_time`)
VALUES (9001, '北航主楼', 39.98404, 116.351129, '北航校园中轴线上的标志性建筑，适合作为时光航迹运营地图中心点。','很多校园影像都以主楼作为空间锚点。', 1, NOW(), NOW()),
       (9002, '新主楼', 39.981038, 116.351763, '教学、科研与校园活动高度集中的综合建筑。', '从不同年代照片中能看到周边道路和景观的变化。', 1, NOW(),NOW()),
       (9003, '晨兴音乐厅', 39.981883, 116.351533, '校园文化活动与演出的重要场所。', '适合承载演出海报、毕业典礼等专题内容。', 1, NOW(), NOW()),
       (9004, '北航图书馆', 39.983832, 116.348728, '学习、检索和校园记忆沉淀的重要空间。', '图书馆周边常出现在学生生活类照片中。', 1, NOW(), NOW()),
       (9005, '学院路校门', 39.984223, 116.352952, '连接校园与学院路城市界面的入口。', '校门是校友返校内容的高频打卡点。',0, NOW(), NOW());

INSERT INTO `media` (`id`, `poi_id`, `type`, `image_path`, `year`, `description`, `upload_user_id`, `review_status`,
                     `reject_reason`, `review_time`, `reviewer_id`, `create_time`, `update_time`)
VALUES (9001, 9001, 'official', '/uploads/dev-seed/buaa-main-1988.svg', 1988, '北航主楼 1988 年资料照片', NULL,
        'approved', NULL, NOW(), 1, NOW(), NOW()),
       (9002, 9001, 'official', '/uploads/dev-seed/buaa-main-2008.svg', 2008, '北航主楼 2008 年资料照片', NULL,
        'approved', NULL, NOW(), 1, NOW(), NOW()),
       (9003, 9002, 'official', '/uploads/dev-seed/buaa-new-main.svg', 2016, '新主楼周边校园景观', NULL, 'approved',
        NULL, NOW(), 1, NOW(), NOW()),
       (9004, 9003, 'official', '/uploads/dev-seed/buaa-concert-hall.svg', 2019, '晨兴音乐厅活动记录', NULL, 'approved',
        NULL, NOW(), 1, NOW(), NOW()),
       (9005, 9004, 'official',
        'https://upload.wikimedia.org/wikipedia/commons/thumb/3/3f/Placeholder_view_vector.svg/640px-Placeholder_view_vector.svg.png',
        2001, '图书馆旧照（远程 URL 示例）', NULL, 'approved', NULL, NOW(), 1, NOW(), NOW()),
       (9006, 9001, 'ugc', '/uploads/dev-seed/buaa-ugc-main.svg', 2024, '用户上传的主楼近景', 9001, 'pending', NULL,
        NULL, NULL, NOW(), NOW());

INSERT INTO `favorite` (`user_id`, `target_type`, `target_id`, `create_time`)
VALUES (9001, 'poi', 9001, NOW()),
       (9002, 'poi', 9001, NOW()),
       (9003, 'poi', 9001, NOW()),
       (9001, 'poi', 9002, NOW()),
       (9002, 'poi', 9003, NOW()),
       (9003, 'media', 9001, NOW());

INSERT INTO `comment` (`user_id`, `target_type`, `target_id`, `content`, `review_status`, `reject_reason`,
                       `review_time`, `reviewer_id`, `create_time`, `update_time`)
VALUES (9001, 'poi', 9001, '第一次来北航就是从主楼开始认路的。', 'approved', NULL, NOW(), 1, NOW(), NOW()),
       (9002, 'poi', 9001, '希望这里可以补充更多九十年代的主楼照片。', 'pending', NULL, NULL, NULL, NOW(), NOW()),
       (9003, 'poi', 9004, '图书馆承载了很多备考和毕业论文的记忆。', 'approved', NULL, NOW(), 1, NOW(), NOW()),
       (9001, 'media', 9001, '这张老照片很有年代感。', 'approved', NULL, NOW(), 1, NOW(), NOW()),
       (9002, 'poi', 9005, '这条测试评论用于验证驳回状态展示。', 'rejected', '测试驳回原因', NOW(), 1, NOW(), NOW());
