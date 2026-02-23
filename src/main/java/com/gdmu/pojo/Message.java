package com.gdmu.pojo;

import lombok.Data;

import java.util.Date;

/**
 * 消息实体类
 */
@Data
public class Message {
    private Long id;                 // 主键ID
    private Long userId;             // 接收用户ID
    private String title;            // 消息标题
    private String content;          // 消息内容
    private Integer type;            // 消息类型：1系统消息，2活动消息，3评价消息
    private Integer isRead;          // 是否已读：0未读，1已读
    private Date createTime;         // 创建时间
}
