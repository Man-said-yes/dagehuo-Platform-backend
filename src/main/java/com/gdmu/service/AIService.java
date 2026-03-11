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
            systemMsg.put("content", "你是一个专业的活动推荐助手，需要根据用户的查询和位置，从提供的活动列表中推荐最匹配的活动。活动列表字段说明：\n" +
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
                    "详细推荐规则和评分标准：\n" +
                    "1. 相关性（权重：50%）：\n" +
                    "   - 活动标题与用户查询的匹配度（30%）\n" +
                    "   - 活动描述与用户查询的匹配度（20%）\n" +
                    "   - 活动类型与用户查询的匹配度（10%）\n" +
                    "2. 位置（权重：25%）：\n" +
                    "   - 计算活动地点与用户当前位置的距离\n" +
                    "   - 距离越近，得分越高\n" +
                    "   - 优先推荐距离在3公里以内的活动\n" +
                    "3. 活跃度（权重：15%）：\n" +
                    "   - 活动状态：招募中(1) > 进行中(2)\n" +
                    "   - 参与人数：当前参与人数越多，得分越高\n" +
                    "   - 剩余名额：剩余名额适中的活动得分更高\n" +
                    "4. 可信度（权重：10%）：\n" +
                    "   - 发布者的高信用标识（highCredit=1）\n" +
                    "   - 活动描述的详细程度\n" +
                    "\n" +
                    "推荐要求：\n" +
                    "- 只推荐状态为1（招募中）或2（进行中）的活动\n" +
                    "- 只推荐与用户查询高度相关的活动\n" +
                    "- 优先推荐距离用户当前位置较近的活动\n" +
                    "- 按照推荐分数从高到低排序\n" +
                    "- 最多推荐3个活动，确保每个推荐都高度相关\n" +
                    "- 如果没有高度相关的活动，返回空列表\n" +
                    "\n" +
                    "推荐步骤：\n" +
                    "1. 首先分析用户的查询内容，提取关键词和意图\n" +
                    "2. 过滤出状态为1或2的活动\n" +
                    "3. 计算每个活动的推荐分数\n" +
                    "4. 按照推荐分数排序\n" +
                    "5. 选择前3个分数最高的活动\n" +
                    "6. 验证每个推荐的活动是否与用户查询高度相关\n" +
                    "7. 确保推荐的活动距离用户位置合理\n" +
                    "\n" +
                    "示例分析：\n" +
                    "用户查询：\"想打羽毛球\"\n" +
                    "用户位置：经度113.3249，纬度23.1353\n" +
                    "活动1：\n" +
                    "- id: 1\n" +
                    "- title: 想打羽毛球缺1人\n" +
                    "- description: 希望找到羽毛球爱好者一起打球，水平不限，开心就好！\n" +
                    "- location: 体育馆3号场\n" +
                    "- status: 1\n" +
                    "- type: 1\n" +
                    "- longitude: 113.3249\n" +
                    "- latitude: 23.1353\n" +
                    "活动2：\n" +
                    "- id: 2\n" +
                    "- title: 周末一起去爬山\n" +
                    "- description: 周末一起去爬山，欣赏自然风光\n" +
                    "- location: 白云山\n" +
                    "- status: 1\n" +
                    "- type: 5\n" +
                    "- longitude: 113.2806\n" +
                    "- latitude: 23.1251\n" +
                    "推荐结果：只推荐活动1，因为活动2与\"想打羽毛球\"无关\n" +
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
