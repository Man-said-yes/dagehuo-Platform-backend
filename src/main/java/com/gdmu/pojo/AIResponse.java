package com.gdmu.pojo;

import lombok.Data;

import java.util.List;

@Data
public class AIResponse {
    private List<Activity> recommendedActivities; // 推荐的活动列表
}
