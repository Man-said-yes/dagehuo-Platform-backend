package com.gdmu.pojo;

import lombok.Data;

import java.util.Date;

/**
 * 系统通知实体类
 */
@Data
public class SystemNotification {
    private Long id;                 // 自增主键（唯一标识一条系统通知）
    private Long userId;             // 外键，关联 users.id（通知接收用户）
    private Long activityId;         // 外键，关联 activity.id（相关活动ID，可为空）
    private Integer notificationType; // 通知类型：1活动创建，2活动结束，3活动取消，4新成员加入，5活动即将开始
    private String title;            // 通知标题
    private String content;          // 通知内容
    private Integer isRead;          // 是否已读：0未读，1已读
    private Date createTime;         // 创建时间
}