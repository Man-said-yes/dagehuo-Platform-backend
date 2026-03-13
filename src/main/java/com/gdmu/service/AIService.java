package com.gdmu.service;

import com.gdmu.pojo.Activity;
import com.gdmu.pojo.AIResponse;
import com.gdmu.pojo.ActivityReport;
import com.gdmu.pojo.AIReportSuggestion;

import java.util.List;

public interface AIService {
    AIResponse getRecommendedActivities(String query, Double longitude, Double latitude, List<Activity> activities);
    AIResponse getInterestRecommendedActivities(Long userId, Double longitude, Double latitude, List<Activity> recruitingActivities, List<Activity> userActivities);
    List<AIReportSuggestion> analyzeReports(List<ActivityReport> reports, List<Activity> activities);
}
