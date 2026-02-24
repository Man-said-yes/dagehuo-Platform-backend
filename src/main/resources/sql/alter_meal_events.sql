-- 使用数据库
USE dagehuo;

-- 为已存在的meal_events表添加活动类型字段
ALTER TABLE meal_events ADD COLUMN IF NOT EXISTS type TINYINT DEFAULT 0 COMMENT '活动类型：0其他，1运动，2约饭，3学习，4游戏，5出行';