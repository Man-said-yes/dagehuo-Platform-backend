-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    openid VARCHAR(100) NOT NULL UNIQUE COMMENT '微信唯一标识',
    student_id VARCHAR(20) UNIQUE COMMENT '学号',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(255) COMMENT '头像URL',
    gender TINYINT DEFAULT 0 COMMENT '性别：0未知，1男，2女',
    phone VARCHAR(20) COMMENT '手机号',
    credit_score INT DEFAULT 100 COMMENT '信誉分：默认100分',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 活动表
CREATE TABLE IF NOT EXISTS activity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    title VARCHAR(100) NOT NULL COMMENT '活动标题',
    description TEXT COMMENT '活动描述',
    event_time DATETIME NOT NULL COMMENT '活动时间',
    location VARCHAR(100) NOT NULL COMMENT '活动地点',
    max_people INT DEFAULT 10 COMMENT '最大参与人数',
    current_people INT DEFAULT 0 COMMENT '当前参与人数',
    status TINYINT DEFAULT 1 COMMENT '活动状态：1招募中，2进行中，3已结束，4已取消',
    type TINYINT DEFAULT 0 COMMENT '活动类型：0其他，1运动，2约饭，3学习，4游戏，5出行',
    creator_id BIGINT NOT NULL COMMENT '创建者ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 参与者表
CREATE TABLE IF NOT EXISTS activity_participant (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    status TINYINT DEFAULT 1 COMMENT '参与状态：1已报名，2已参与，3已取消',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE (activity_id, user_id),
    FOREIGN KEY (activity_id) REFERENCES activity(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 消息表
CREATE TABLE IF NOT EXISTS messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '接收用户ID',
    title VARCHAR(100) NOT NULL COMMENT '消息标题',
    content TEXT NOT NULL COMMENT '消息内容',
    type TINYINT DEFAULT 1 COMMENT '消息类型：1系统消息，2活动消息，3评价消息',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读：0未读，1已读',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 评价表
CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    reviewer_id BIGINT NOT NULL COMMENT '评价者ID',
    reviewed_id BIGINT NOT NULL COMMENT '被评价者ID',
    rating TINYINT NOT NULL COMMENT '评分：1-5星',
    content TEXT COMMENT '评价内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (activity_id) REFERENCES activity(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewed_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 插入测试数据
INSERT INTO users (openid, student_id, nickname, avatar, gender) VALUES
('test_openid_001', '24000000001', '张三', 'https://example.com/avatar1.jpg', 1),
('test_openid_002', '24000000002', '李四', 'https://example.com/avatar2.jpg', 2),
('test_openid_003', '24000000003', '王五', 'https://example.com/avatar3.jpg', 1);

-- 插入测试活动
INSERT INTO activity (title, description, event_time, location, max_people, current_people, status, type, creator_id) VALUES
('想打羽毛球缺1人', '希望找到羽毛球爱好者一起打球，水平不限，开心就好！', '2024-12-31 19:00:00', '体育馆3号场', 3, 1, 1, 1, 1),
('周末一起去图书馆学习', '期末复习，寻找志同道合的同学一起学习', '2024-12-28 14:00:00', '图书馆二楼', 5, 1, 1, 3, 2),
('约饭：食堂三楼', '想找个人一起吃饭，聊天', '2024-12-27 12:00:00', '食堂三楼', 2, 1, 1, 2, 3);

-- 插入测试参与者
INSERT INTO activity_participant (activity_id, user_id, status) VALUES
(1, 1, 1),
(2, 2, 1),
(3, 3, 1);
