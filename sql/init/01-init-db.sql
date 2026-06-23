-- CloudBlog 初始化脚本
-- 统一使用 springcloud_db 数据库

CREATE DATABASE IF NOT EXISTS springcloud_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE springcloud_db;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`    VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`    VARCHAR(255) NOT NULL COMMENT '加密密码',
    `nickname`    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `avatar_url`  VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `signature`   VARCHAR(200) DEFAULT NULL COMMENT '个性签名',
    `email`       VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 博客表
CREATE TABLE IF NOT EXISTS `blog` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '博客ID',
    `user_id`     BIGINT       NOT NULL COMMENT '作者ID',
    `title`       VARCHAR(100) NOT NULL COMMENT '标题',
    `content`     TEXT         NOT NULL COMMENT '内容',
    `summary`     VARCHAR(300) DEFAULT NULL COMMENT '摘要',
    `cover_image` VARCHAR(255) DEFAULT NULL COMMENT '封面图URL',
    `like_count`  INT          NOT NULL DEFAULT 0 COMMENT '点赞数',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0-草稿 1-已发布',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='博客表';

-- 博客点赞表
CREATE TABLE IF NOT EXISTS `blog_like` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `blog_id`    BIGINT   NOT NULL COMMENT '博客ID',
    `user_id`    BIGINT   NOT NULL COMMENT '用户ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_blog_user` (`blog_id`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='博客点赞表';

-- 评论表
CREATE TABLE IF NOT EXISTS `comment` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `blog_id`    BIGINT       NOT NULL COMMENT '博客ID',
    `user_id`    BIGINT       NOT NULL COMMENT '评论者ID',
    `parent_id`  BIGINT       DEFAULT NULL COMMENT '父评论ID（回复评论用）',
    `content`    VARCHAR(500) NOT NULL COMMENT '评论内容',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_blog_id` (`blog_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 评论点赞表
CREATE TABLE IF NOT EXISTS `comment_like` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `comment_id` BIGINT   NOT NULL COMMENT '评论ID',
    `user_id`    BIGINT   NOT NULL COMMENT '用户ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论点赞表';
