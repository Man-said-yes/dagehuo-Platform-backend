package com.gdmu.service;

import com.gdmu.pojo.Activity;
import com.gdmu.pojo.AIResponse;

import java.util.List;

public interface AIService {
    AIResponse getRecommendedActivities(String query, Double longitude, Double latitude, List<Activity> activities);
    AIResponse getInterestRecommendedActivities(Long userId, Double longitude, Double latitude, List<Activity> recruitingActivities, List<Activity> userActivities);
}
