package com.gdmu.pojo;

import lombok.Data;

import java.util.Date;

/**
 * 搭伙活动实体类
 */
@Data
public class MealEvent {
    private Long id;                 // 主键ID
    private String title;            // 活动标题
    private String description;      // 活动描述
    private Date eventTime;          // 活动时间
    private String location;         // 活动地点
    private Integer maxPeople;       // 最大参与人数
    private Integer currentPeople;   // 当前参与人数
    private Integer status;          // 活动状态：1招募中，2进行中，3已结束，4已取消
    private Long creatorId;          // 创建者ID
    private Date createTime;         // 创建时间
    private Date updateTime;         // 更新时间
}
