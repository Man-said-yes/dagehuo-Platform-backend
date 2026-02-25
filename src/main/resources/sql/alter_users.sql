-- 使用数据库
USE dagehuo;

-- 为已存在的users表添加信誉分字段
ALTER TABLE users ADD COLUMN IF NOT EXISTS credit_score INT DEFAULT 100 COMMENT '信誉分：默认100分';