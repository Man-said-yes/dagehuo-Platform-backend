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
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
            // 1. 前置过滤：只保留招募中的活动（减少AI处理量）
            List<Activity> recruitingActivities = activities.stream()
                    .filter(activity -> activity.getStatus() == 1)
                    .collect(Collectors.toList());
            if (recruitingActivities.isEmpty()) {
                AIResponse emptyResponse = new AIResponse();
                emptyResponse.setRecommendedActivities(new ArrayList<>());
                return emptyResponse;
            }

            // 2. 构建请求参数（核心优化：精简Prompt+强制JSON+降低随机性）
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "xop3qwencodernext");

            JSONArray messages = new JSONArray();

            // 系统消息：让AI自己判断活动相关性
            JSONObject systemMsg = new JSONObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", "### 核心规则\n" +
                    "1. 仅返回JSON格式，无任何额外文字，格式：{\"recommendIds\":[1,2,3]}\n" +
                    "2. 根据用户查询内容，推荐最相关的活动\n" +
                    "3. 优先推荐距离用户（经度"+longitude+",纬度"+latitude+"）3公里内的活动\n" +
                    "4. 无匹配活动返回：{\"recommendIds\":[]}\n" +
                    "### 活动字段说明\n" +
                    "- id: 活动ID\n" +
                    "- title: 活动标题\n" +
                    "- description: 活动描述\n" +
                    "- type: 活动类型（0其他，1运动，2约饭，3学习，4游戏，5出行）\n" +
                    "- longitude: 活动地点经度\n" +
                    "- latitude: 活动地点纬度\n" +
                    "- highCredit: 高信用标识（1表示该活动为高信用活动，0表示普通活动）");
            messages.put(systemMsg);

            // 用户消息：活动列表拼接成句子（AI理解更准确）
            JSONObject userMsg = new JSONObject();
            StringBuilder activitiesStr = new StringBuilder();
            for (Activity activity : recruitingActivities) {
                String typeStr = getTypeString(activity.getType());
                String highCreditStr = activity.getHighCredit() == 1 ? "高信用" : "普通";
                // 计算距离
                double distance = 0.0;
                if (activity.getLongitude() != null && activity.getLatitude() != null) {
                    distance = calculateDistance(latitude, longitude, activity.getLatitude(), activity.getLongitude());
                }
                activitiesStr.append("活动ID " + activity.getId() + "：" + activity.getTitle() + "，" + activity.getDescription() + "，地点：" + activity.getLocation() + "，类型：" + typeStr + "，距离：" + String.format("%.2f", distance) + "公里，信用等级：" + highCreditStr + "\n");

            }
            userMsg.put("role", "user");
            userMsg.put("content", "用户需求：用户想要" + query + "，当前位置经度 " + longitude + "，纬度 " + latitude + "\n请根据用户需求推荐最相关的活动：\n" + activitiesStr.toString());
            messages.put(userMsg);

            requestBody.put("messages", messages);
            requestBody.put("stream", false);
            requestBody.put("temperature", 0.1); // 关键：降低随机性，精准匹配
            requestBody.put("top_p", 0.1); // 补充：限制AI仅选高匹配结果
            requestBody.put("max_tokens", 2048);
            requestBody.put("stream_options", new JSONObject().put("include_usage", true));

            // 3. 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", "Bearer " + API_KEY);

            // 4. 发送请求
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody.toString(), headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    API_URL,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            // 5. 解析响应（核心优化：JSON解析+关键词兜底）
            String responseBodyStr = responseEntity.getBody();
            log.info("AI API响应内容: {}", responseBodyStr);

            List<Activity> recommendedActivities = new ArrayList<>();
            try {
                JSONObject responseBody = new JSONObject(responseBodyStr);
                JSONArray choices = responseBody.getJSONArray("choices");

                if (choices.length() > 0) {
                    JSONObject choice = choices.getJSONObject(0);
                    JSONObject message = choice.getJSONObject("message");
                    String content = message.getString("content");
                    log.info("AI推荐内容: {}", content);

                    // 提取纯JSON（容错：AI可能加多余文字）
                    String pureJson = extractPureJson(content);
                    JSONObject aiResult = new JSONObject(pureJson);
                    JSONArray recommendIds = aiResult.getJSONArray("recommendIds");

                    // 匹配原始活动
                    for (int i = 0; i < recommendIds.length(); i++) {
                        Long recId = recommendIds.getLong(i);
                        for (Activity activity : recruitingActivities) {
                            if (activity.getId().equals(recId) && !recommendedActivities.contains(activity)) {
                                recommendedActivities.add(activity);
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("解析AI JSON失败: {}", e.getMessage());
                // 兜底：关键词匹配（保证至少返回相关活动）
                recommendedActivities = recruitingActivities.stream()
                        .filter(act -> act.getTitle().contains(query) || act.getDescription().contains(query))
                        .collect(Collectors.toList());
            }

            // 最终返回：去重+保证只返回招募中活动
            recommendedActivities = recommendedActivities.stream()
                    .filter(act -> act.getStatus() == 1)
                    .distinct()
                    .collect(Collectors.toList());

            AIResponse aiResponse = new AIResponse();
            aiResponse.setRecommendedActivities(recommendedActivities);
            return aiResponse;

        } catch (Exception e) {
            log.error("AI推荐服务异常: {}", e.getMessage(), e);
            AIResponse aiResponse = new AIResponse();
            aiResponse.setRecommendedActivities(new ArrayList<>());
            return aiResponse;
        }
    }

    // 工具方法：提取纯JSON（容错AI返回多余文字）
    private String extractPureJson(String rawContent) {
        int start = rawContent.indexOf("{");
        int end = rawContent.lastIndexOf("}");
        if (start != -1 && end != -1) {
            return rawContent.substring(start, end + 1);
        }
        return "{\"recommendIds\":[]}";
    }
    
    // 工具方法：将活动类型数字转换为类型名称
    private String getTypeString(Integer type) {
        switch (type) {
            case 0: return "其他";
            case 1: return "运动";
            case 2: return "约饭";
            case 3: return "学习";
            case 4: return "游戏";
            case 5: return "出行";
            default: return "其他";
        }
    }
    
    // 工具方法：计算两点之间的距离（单位：公里）
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 地球半径（公里）
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}