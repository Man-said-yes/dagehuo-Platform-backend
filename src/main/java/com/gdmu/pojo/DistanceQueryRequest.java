package com.gdmu.pojo;

import lombok.Data;

@Data
public class DistanceQueryRequest {
    private Double longitude; // 用户当前位置的经度
    private Double latitude; // 用户当前位置的纬度
    private Integer type; // 活动类型：0其他，1运动，2约饭，3学习，4游戏，5出行
    private Integer page = 1; // 页码，默认1
    private Integer pageSize = 10; // 每页数量，默认10
}