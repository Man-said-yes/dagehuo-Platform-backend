-- 使用数据库
USE dagehuo;

-- 修改表名：meal_events -> activity
ALTER TABLE meal_events RENAME TO activity;

-- 修改表名：participants -> activity_participant
ALTER TABLE participants RENAME TO activity_participant;

-- 更新外键约束中的表名
ALTER TABLE activity_participant CHANGE COLUMN meal_event_id activity_id BIGINT NOT NULL COMMENT '活动ID';

-- 更新评价表中的外键约束
ALTER TABLE reviews CHANGE COLUMN meal_event_id activity_id BIGINT NOT NULL COMMENT '活动ID';

-- 更新外键引用
ALTER TABLE activity_participant DROP FOREIGN KEY activity_participant_ibfk_1;
ALTER TABLE activity_participant ADD FOREIGN KEY (activity_id) REFERENCES activity(id) ON DELETE CASCADE;

ALTER TABLE reviews DROP FOREIGN KEY reviews_ibfk_1;
ALTER TABLE reviews ADD FOREIGN KEY (activity_id) REFERENCES activity(id) ON DELETE CASCADE;