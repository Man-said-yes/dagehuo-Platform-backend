package com.gdmu.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class CreateEventRequest {
    private Integer type;       // 活动类型：0其他，1运动，2约饭，3学习，4游戏，5出行
    private String title;       // 活动标题
    private String description; // 活动描述
    private String location;    // 活动地点
    private Date eventTime;     // 活动时间
    private Integer maxPeople;  // 最大参与人数
}