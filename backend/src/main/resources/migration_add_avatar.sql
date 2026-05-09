-- 为 user_account 表添加 avatar 字段
-- 用于存储用户头像（支持 base64 Data URL 或图片路径）

ALTER TABLE user_account
ADD COLUMN IF NOT EXISTS avatar TEXT DEFAULT NULL COMMENT '用户头像URL' AFTER phone;
