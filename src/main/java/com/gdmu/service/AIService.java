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
            // 构建请求参数
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "xop3qwen1b7"); // 使用正确的模型ID
            
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
                    "\n" +
                    "推荐规则和评分标准：\n" +
                    "1. 相关性（权重：40%）：活动标题和描述与用户查询的匹配度\n" +
                    "2. 位置（权重：30%）：活动地点与用户当前位置的距离远近\n" +
                    "3. 活跃度（权重：20%）：活动的参与人数和状态，优先推荐招募中和进行中的活动\n" +
                    "4. 可信度（权重：10%）：发布者的高信用标识\n" +
                    "\n" +
                    "推荐要求：\n" +
                    "- 只推荐状态为1（招募中）或2（进行中）的活动\n" +
                    "- 按照推荐分数从高到低排序\n" +
                    "- 最多推荐5个活动\n" +
                    "- 确保推荐的活动与用户查询高度相关\n" +
                    "- 考虑用户的位置信息，优先推荐距离较近的活动\n" +
                    "\n" +
                    "请根据用户的查询内容、位置信息和活动的相关属性，返回最匹配的活动列表。\n" +
                    "请直接返回活动列表，每个活动包含id、title和description字段，格式如下：\n" +
                    "活动ID: 1\n" +
                    "活动标题: 想打羽毛球缺1人\n" +
                    "活动描述: 希望找到羽毛球爱好者一起打球，水平不限，开心就好！\n" +
                    "\n" +
                    "活动ID: 2\n" +
                    "活动标题: 周末一起去爬山\n" +
                    "活动描述: 周末一起去爬山，欣赏自然风光\n");


            messages.put(systemMsg);
            
            // 用户消息，包含查询和位置
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", "用户查询：" + query + "\n用户位置：经度 " + longitude + "，纬度 " + latitude + "\n活动列表：" + activities.toString());
            messages.put(userMsg);
            
            requestBody.put("messages", messages);
            requestBody.put("stream", false);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 4096);
            requestBody.put("stream_options", new JSONObject().put("include_usage", true));
            
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
            String responseBodyStr = responseEntity.getBody();
            log.info("AI API响应内容: {}", responseBodyStr);
            
            List<Activity> recommendedActivities = new ArrayList<>();
            try {
                JSONObject responseBody = new JSONObject(responseBodyStr);
                JSONArray choices = responseBody.getJSONArray("choices");
                
                // 提取推荐的活动
                for (int i = 0; i < choices.length(); i++) {
                    JSONObject choice = choices.getJSONObject(i);
                    JSONObject message = choice.getJSONObject("message");
                    String content = message.getString("content");
                    log.info("AI推荐内容: {}", content);
                    
                    // 尝试解析活动列表
                    try {
                        // 解析文本格式的活动列表
                        String[] activityBlocks = content.split("\n\n");
                        for (String block : activityBlocks) {
                            if (block.trim().isEmpty()) continue;
                            
                            // 提取活动ID、标题和描述
                            String[] lines = block.split("\n");
                            Long id = null;
                            String title = "";
                            String description = "";
                            
                            for (String line : lines) {
                                line = line.trim();
                                if (line.startsWith("活动ID:")) {
                                    try {
                                        id = Long.parseLong(line.substring(5).trim());
                                    } catch (Exception e) {
                                        // 忽略解析错误
                                    }
                                } else if (line.startsWith("活动标题:")) {
                                    title = line.substring(5).trim();
                                } else if (line.startsWith("活动描述:")) {
                                    description = line.substring(5).trim();
                                }
                            }
                            
                            // 如果成功提取了ID，添加到推荐列表
                            if (id != null) {
                                // 查找对应的活动
                                for (Activity activity : activities) {
                                    if (activity.getId().equals(id)) {
                                        recommendedActivities.add(activity);
                                        break;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("解析AI推荐内容失败: {}", e.getMessage());
                        // 如果解析失败，返回所有活动
                        recommendedActivities = activities;
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("解析AI API响应失败: {}", e.getMessage());
                // 如果API响应解析失败，返回所有活动
                recommendedActivities = activities;
            }
            
            // 如果没有推荐活动，返回所有活动
            if (recommendedActivities.isEmpty()) {
                recommendedActivities = activities;
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
