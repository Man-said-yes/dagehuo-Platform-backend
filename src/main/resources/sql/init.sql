-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS dagehuo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE dagehuo;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    openid VARCHAR(100) NOT NULL UNIQUE COMMENT '微信唯一标识',
    student_id VARCHAR(20) UNIQUE COMMENT '学号',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(255) COMMENT '头像URL',
    gender TINYINT DEFAULT 0 COMMENT '性别：0未知，1男，2女',
    phone VARCHAR(20) COMMENT '手机号',
    password VARCHAR(100) COMMENT '密码（管理员登录用）',
    credit_score INT DEFAULT 100 COMMENT '信誉分：默认100分',
    high_credit TINYINT DEFAULT 0 COMMENT '高信誉分标识：0否，1是',
    role VARCHAR(20) DEFAULT 'user' COMMENT '角色：user普通用户，admin管理员',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信息表';

-- 活动表
CREATE TABLE IF NOT EXISTS activity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    title VARCHAR(100) NOT NULL COMMENT '活动标题',
    description TEXT COMMENT '活动描述',
    event_time DATETIME NOT NULL COMMENT '活动时间',
    location VARCHAR(100) NOT NULL COMMENT '活动地点',
    campus VARCHAR(50) COMMENT '校区',
    longitude DOUBLE COMMENT '经度',
    latitude DOUBLE COMMENT '纬度',
    max_people INT DEFAULT 10 COMMENT '最大参与人数',
    current_people INT DEFAULT 0 COMMENT '当前参与人数',
    status TINYINT DEFAULT 1 COMMENT '活动状态：1招募中，2进行中，3已结束，4已取消',
    type TINYINT DEFAULT 0 COMMENT '活动类型：0其他，1运动，2约饭，3学习，4游戏，5出行',
    high_credit TINYINT DEFAULT 0 COMMENT '高信用标识：0否，1是',
    creator_id BIGINT NOT NULL COMMENT '创建者ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动表';

-- 参与者表
CREATE TABLE IF NOT EXISTS activity_participant (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    status TINYINT DEFAULT 1 COMMENT '参与状态：1已报名，2已参与，3已取消',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_activity_user (activity_id, user_id),
    FOREIGN KEY (activity_id) REFERENCES activity(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='参与者表';

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';

-- 聊天群表
CREATE TABLE IF NOT EXISTS chat_group (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '群聊ID',
    name VARCHAR(100) NOT NULL COMMENT '群聊名称',
    type TINYINT DEFAULT 1 COMMENT '群聊类型：1-系统消息群, 2-活动群, 3-自定义群',
    activity_id BIGINT COMMENT '关联的活动ID（如果是活动群）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (activity_id) REFERENCES activity(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天群表';

-- 群成员表
CREATE TABLE IF NOT EXISTS chat_group_member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    group_id BIGINT NOT NULL COMMENT '群聊ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    join_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    last_read_message_id BIGINT COMMENT '最后已读消息ID',
    UNIQUE KEY uk_group_user (group_id, user_id),
    FOREIGN KEY (group_id) REFERENCES chat_group(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群成员表';

-- 聊天消息表
CREATE TABLE IF NOT EXISTS chat_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    group_id BIGINT NOT NULL COMMENT '群聊ID',
    user_id BIGINT COMMENT '发送者ID（系统消息为null）',
    content TEXT NOT NULL COMMENT '消息内容',
    type TINYINT DEFAULT 1 COMMENT '消息类型：1-文本, 2-系统消息',
    send_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    FOREIGN KEY (group_id) REFERENCES chat_group(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';

-- 初始化系统消息群
INSERT INTO chat_group (name, type) VALUES ('系统消息', 1);

-- 用户信誉分记录
CREATE TABLE IF NOT EXISTS user_credit_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    credit_change INT NOT NULL COMMENT '信誉分变化值（正数加分，负数扣分）',
    reason VARCHAR(100) NOT NULL COMMENT '变化原因',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信誉分记录表';

-- 活动举报表
CREATE TABLE IF NOT EXISTS activity_report (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '自增主键（唯一标识一条举报记录）',
    activity_id BIGINT NOT NULL COMMENT '外键，关联 activity.id（哪个活动被举报）',
    reporter_user_id BIGINT NOT NULL COMMENT '外键，关联 users.id（谁举报的，记录用户 ID 保护隐私）',
    report_reason VARCHAR(255) NOT NULL COMMENT '举报理由（如 "活动虚假""涉违规内容"，前端可做下拉选择 + 自定义输入）',
    report_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '举报时间（自动填充当前时间）',
    handle_status TINYINT(1) DEFAULT 0 COMMENT '处理状态：0 = 未处理，1 = 已核实（下架活动），2 = 已驳回（举报不成立）',
    handle_time DATETIME COMMENT '处理时间（处理时自动填充）',
    FOREIGN KEY (activity_id) REFERENCES activity(id) ON DELETE CASCADE,
    FOREIGN KEY (reporter_user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动举报表';

-- 系统通知表
CREATE TABLE IF NOT EXISTS system_notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '自增主键（唯一标识一条系统通知）',
    user_id BIGINT NOT NULL COMMENT '外键，关联 users.id（通知接收用户）',
    activity_id BIGINT COMMENT '外键，关联 activity.id（相关活动ID，可为空）',
    notification_type TINYINT NOT NULL COMMENT '通知类型：1活动创建，2活动结束，3活动取消，4新成员加入，5活动即将开始',
    title VARCHAR(100) NOT NULL COMMENT '通知标题',
    content TEXT NOT NULL COMMENT '通知内容',
    is_read TINYINT(1) DEFAULT 0 COMMENT '是否已读：0未读，1已读',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (activity_id) REFERENCES activity(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统通知表';
