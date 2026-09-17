-- campus-activity-api 表结构
--
-- 库：campus_db
-- 字符集：utf8mb4 / utf8mb4_unicode_ci
--
-- 初始化：
--   CREATE DATABASE campus_db DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
--   mysql -u root -p campus_db < sql/schema.sql
--
-- 应用账号：不要用 root 跑应用。建一个只授权 campus_db 的账号：
--   CREATE USER 'campus_app'@'127.0.0.1' IDENTIFIED BY '<你的密码>';
--   GRANT ALL PRIVILEGES ON campus_db.* TO 'campus_app'@'127.0.0.1';
--
-- 注意 host 写 '127.0.0.1' 而不是 '%'：MySQL 按 user@host 匹配，
-- 写成 '%' 表示允许任意来源连接，本机开发没必要开这么大。

-- ---------------------------------------------------------------
-- 活动表
-- ---------------------------------------------------------------
CREATE TABLE `activity` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '活动标题',
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '活动描述',
  `location` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '活动地点',
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `capacity` int NOT NULL COMMENT '总名额',
  `enrolled_count` int NOT NULL DEFAULT '0' COMMENT '已报名数',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0未发布 1已发布 2已取消',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status_start` (`status`,`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动表';

-- ---------------------------------------------------------------
-- 活动报名表
--
-- active_flag 是 STORED 生成列，不由应用写入：
--   报名有效（status IN (0,1)）时等于 user_id，取消/作废后变成 NULL。
--   配合唯一索引 uk_activity_active(activity_id, active_flag) 实现
--   "同一人对同一活动只能有一条有效报名，但取消后可以重新报名"——
--   因为唯一索引里 NULL 可以无限重复（NULL = NULL 的结果是 UNKNOWN 而不是 TRUE）。
--
-- 教训：往 Entity 里加这个字段、或手动给它赋值，都是错的
--       （生成列硬写会报 ERROR 3105）。
-- ---------------------------------------------------------------
CREATE TABLE `activity_enrollment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `activity_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0待支付 1已确认 2已取消',
  `expire_time` datetime DEFAULT NULL COMMENT '支付截止时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `active_flag` bigint GENERATED ALWAYS AS (if((`status` in (0,1)),`user_id`,NULL)) STORED,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_active` (`activity_id`,`active_flag`),
  KEY `idx_status_expire` (`status`,`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动报名表';

-- ---------------------------------------------------------------
-- 用户表
--
-- role：0 普通用户 / 1 管理员。登录时会被写进 JWT payload，
--       所以改角色后要等 token 过期（30 分钟）才生效。
-- ---------------------------------------------------------------
CREATE TABLE `users` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码密文',
  `nickname` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '昵称',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1启用，0禁用',
  `role` tinyint NOT NULL DEFAULT '0' COMMENT '角色：0普通用户，1管理员',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
