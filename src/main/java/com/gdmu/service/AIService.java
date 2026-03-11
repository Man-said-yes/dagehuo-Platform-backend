package com.gdmu.service;

import com.gdmu.pojo.Activity;
import com.gdmu.pojo.AIResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Service
public class AIService {
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${ai.spark.api-url}")
    private String API_URL;
    
    @Value("${ai.spark.api-key}")
    private String API_KEY;
    
    @Value("${ai.spark.app-id}")
    private String APP_ID;

    public AIResponse getRecommendedActivities(String query, Double longitude, Double latitude, List<Activity> activities) {
        try {
            // 构建请求参数
            JSONObject requestBody = new JSONObject();
            requestBody.put("app_id", APP_ID);
            requestBody.put("uid", "user_" + System.currentTimeMillis());
            
            JSONArray messages = new JSONArray();
            
            // 系统消息，包含活动列表字段解释
            JSONObject systemMsg = new JSONObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", "你是一个活动推荐助手，需要根据用户的查询和位置，从提供的活动列表中推荐最匹配的活动。活动列表字段说明：\n" +
                    "- id: 活动ID\n" +
                    "- title: 活动标题\n" +
                    "- description: 活动描述\n" +
                    "- eventTime: 活动时间\n" +
                    "- location: 活动地点\n" +
                    "- campus: 校区\n" +
                    "- longitude: 经度\n" +
                    "- latitude: 纬度\n" +
                    "- maxPeople: 最大参与人数\n" +
                    "- currentPeople: 当前参与人数\n" +
                    "- status: 活动状态（1招募中，2进行中，3已结束，4已取消）\n" +
                    "- type: 活动类型（0其他，1运动，2约饭，3学习，4游戏，5出行）\n" +
                    "- highCredit: 高信用标识（0否，1是）\n" +
                    "请根据用户的查询内容、位置信息和活动的相关属性，返回最匹配的活动列表。");
            messages.put(systemMsg);
            
            // 用户消息，包含查询和位置
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", "用户查询：" + query + "\n用户位置：经度 " + longitude + "，纬度 " + latitude + "\n活动列表：" + activities.toString());
            messages.put(userMsg);
            
            requestBody.put("messages", messages);
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", "Bearer " + API_KEY);
            
            // 发送请求
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody.toString(), headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    API_URL,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );
            
            // 解析响应
            JSONObject responseBody = new JSONObject(responseEntity.getBody());
            JSONArray choices = responseBody.getJSONObject("result").getJSONArray("choices");
            
            // 提取推荐的活动
            List<Activity> recommendedActivities = new ArrayList<>();
            for (int i = 0; i < choices.length(); i++) {
                JSONObject choice = choices.getJSONObject(i);
                JSONObject message = choice.getJSONObject("message");
                String content = message.getString("content");
                // 这里需要根据AI返回的格式解析活动列表
                // 假设AI返回的是JSON格式的活动列表
                JSONArray activityArray = new JSONArray(content);
                for (int j = 0; j < activityArray.length(); j++) {
                    JSONObject activityObj = activityArray.getJSONObject(j);
                    Activity activity = new Activity();
                    activity.setId(activityObj.getLong("id"));
                    activity.setTitle(activityObj.getString("title"));
                    activity.setDescription(activityObj.getString("description"));
                    // 其他字段的解析...
                    recommendedActivities.add(activity);
                }
            }
            
            AIResponse aiResponse = new AIResponse();
            aiResponse.setRecommendedActivities(recommendedActivities);
            return aiResponse;
        } catch (Exception e) {
            e.printStackTrace();
            // 处理错误，返回空列表
            AIResponse aiResponse = new AIResponse();
            aiResponse.setRecommendedActivities(new ArrayList<>());
            return aiResponse;
        }
    }
}
