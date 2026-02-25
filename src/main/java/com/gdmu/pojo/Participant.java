package com.gdmu.pojo;

import lombok.Data;

import java.util.Date;

/**
 * 参与者实体类
 */
@Data
public class Participant {
    private Long id;                 // 主键ID
    private Long activityId;        // 活动ID
    private Long userId;             // 用户ID
    private Integer status;          // 参与状态：1已报名，2已参与，3已取消
    private Date createTime;         // 创建时间
    private Date updateTime;         // 更新时间
}
