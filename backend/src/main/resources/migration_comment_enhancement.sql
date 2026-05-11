-- 圈子评论系统增强迁移脚本
-- 日期: 2026-05-10
-- 功能:
--   1. 为 circle_comment 表添加审核状态、嵌套回复、点赞计数支持
--   2. 创建评论点赞表 circle_comment_like

-- 1. 升级 circle_comment 表结构
ALTER TABLE circle_comment
  ADD COLUMN parent_id BIGINT NULL DEFAULT NULL AFTER post_id,
  ADD COLUMN reply_to_user_name VARCHAR(64) NULL DEFAULT NULL AFTER user_name,
  ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING' AFTER content,
  ADD COLUMN like_count INT NOT NULL DEFAULT 0 AFTER status;

-- 添加索引以优化查询性能
ALTER TABLE circle_comment
  ADD INDEX idx_parent_id (parent_id),
  ADD INDEX idx_status (status);

-- 将现有评论状态设置为已通过（向后兼容）
UPDATE circle_comment SET status = 'APPROVED' WHERE status IS NULL OR status = '';

-- 2. 创建评论点赞表
CREATE TABLE IF NOT EXISTS circle_comment_like (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  comment_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_comment_user (comment_id, user_id),
  INDEX idx_comment_id (comment_id),
  INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
