-- 评价嵌套回复功能迁移脚本
-- 执行前请备份数据

-- 1. 添加新字段
ALTER TABLE review ADD COLUMN parent_id BIGINT NULL DEFAULT NULL AFTER images;
ALTER TABLE review ADD COLUMN reply_to_user_id BIGINT NULL DEFAULT NULL AFTER parent_id;
ALTER TABLE review ADD COLUMN reply_to_user_name VARCHAR(64) NULL DEFAULT NULL AFTER reply_to_user_id;

-- 2. 添加索引
ALTER TABLE review ADD INDEX idx_parent_id (parent_id);

-- 3. 迁移现有 reply 数据到新的嵌套结构
-- 注意：这个迁移需要根据实际情况调整
-- 现有的 reply 字段保留，用于向后兼容

-- 查看迁移结果
SELECT 
    id, 
    item_id, 
    buyer_id, 
    seller_id,
    content, 
    reply,
    parent_id,
    reply_to_user_id,
    reply_to_user_name,
    status
FROM review 
LIMIT 10;
