package com.gdmu.pojo;

import lombok.Data;

@Data
public class AISearchRequest {
    private String query;       // 用户查询语句
    private Double longitude;   // 经度
    private Double latitude;    // 纬度
}
