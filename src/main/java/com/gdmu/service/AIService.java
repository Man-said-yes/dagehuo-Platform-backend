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
                    "- 活动ID: 活动的唯一标识\n" +
                    "- 标题: 活动的标题\n" +
                    "- 描述: 活动的详细描述\n" +
                    "- 地点: 活动的具体地点\n" +
                    "- 校区: 活动所在的校区\n" +
                    "- 经度: 活动地点的经度\n" +
                    "- 纬度: 活动地点的纬度\n" +
                    "- 类型: 活动类型（0其他，1运动，2约饭，3学习，4游戏，5出行）\n" +
                    "- 高信用: 高信用标识（0否，1是）\n" +
                    "\n" +
                    "详细推荐规则和评分标准：\n" +
                    "1. 相关性（权重：70%）：\n" +
                    "   - 活动标题与用户查询的匹配度（70%）\n" +
                    "   - 活动描述与用户查询的匹配度（20%）\n" +
                    "   - 活动类型与用户查询的匹配度（10%）\n" +
                    "2. 位置（权重：15%）：\n" +
                    "   - 计算活动地点与用户当前位置的距离\n" +
                    "   - 距离越近，得分越高\n" +
                    "   - 优先推荐距离在3公里以内的活动\n" +
                    "3. 活跃度（权重：15%）：\n" +
                    "   - 高信用活动优先推荐\n" +
                    "   - 距离用户位置近的活动优先推荐\n" +
                    "\n" +
                    "推荐要求：\n" +
                    "- 只推荐与用户查询高度相关的活动，不相关的活动不要推荐\n" +
                    "- 优先推荐距离用户当前位置较近的活动\n" +
                    "- 按照推荐分数从高到低排序\n" +
                    "- 只推荐最相关的活动，数量不限但必须是真正相关的\n" +
                    "- 如果没有高度相关的活动，返回空列表\n" +
                    "\n" +
                    "推荐步骤：\n" +
                    "1. 首先分析用户的查询内容，提取关键词和意图\n" +
                    "2. 计算每个活动与用户查询的相关性得分\n" +
                    "3. 只保留相关性得分高的活动（与用户查询真正相关的活动）\n" +
                    "4. 按照推荐分数排序\n" +
                    "5. 确保推荐的活动距离用户位置合理\n" +
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
                    "活动3：\n" +
                    "- id: 3\n" +
                    "- title: 羽毛球友谊赛\n" +
                    "- description: 组织一场羽毛球友谊赛，欢迎各位爱好者参加\n" +
                    "- location: 羽毛球馆\n" +
                    "- status: 2\n" +
                    "- type: 1\n" +
                    "- longitude: 113.3249\n" +
                    "- latitude: 23.1353\n" +
                    "推荐结果：只推荐活动1，因为活动2与\"想打羽毛球\"无关，活动3状态不是招募中\n" +
                    "\n" +
                    "请根据用户的查询内容、位置信息和活动的相关属性，严格筛选并返回最匹配的活动列表。\n" +
                    "请只返回活动ID列表，每个ID占一行，只包含数字，不要包含任何其他文字或格式，例如：\n" +
                    "18\n" +
                    "25\n" +
                    "32\n" +
                    "\n" +
                    "如果没有相关的活动，请返回空列表，不要返回任何内容。\n" +
                    "重要：请确保每个ID占一行，不要在同一行返回多个ID，也不要添加任何描述性文字。\n");



            messages.put(systemMsg);
            
            // 过滤出招募中的活动
            List<Activity> recruitingActivities = activities.stream()
                    .filter(activity -> activity.getStatus() == 1)
                    .collect(java.util.stream.Collectors.toList());
            
            // 构建活动列表字符串，移除不需要的字段
            StringBuilder activitiesStr = new StringBuilder();
            for (Activity activity : recruitingActivities) {
                activitiesStr.append("活动ID: " + activity.getId() + "\n");
                activitiesStr.append("标题: " + activity.getTitle() + "\n");
                activitiesStr.append("描述: " + activity.getDescription() + "\n");
                activitiesStr.append("地点: " + activity.getLocation() + "\n");
                activitiesStr.append("校区: " + activity.getCampus() + "\n");
                activitiesStr.append("经度: " + activity.getLongitude() + "\n");
                activitiesStr.append("纬度: " + activity.getLatitude() + "\n");
                activitiesStr.append("类型: " + activity.getType() + "\n");
                activitiesStr.append("高信用: " + activity.getHighCredit() + "\n\n");
            }
            
            // 用户消息，包含查询和位置
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", "用户查询：" + query + "\n用户位置：经度 " + longitude + "，纬度 " + latitude + "\n活动列表：\n" + activitiesStr.toString());
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
                    
                    // 尝试解析活动ID列表
                    try {
                        // 清理内容，只保留数字部分
                        String cleanedContent = content.replaceAll("[^0-9\\n]", "");
                        // 按行分割
                        String[] lines = cleanedContent.split("\n");
                        for (String line : lines) {
                            line = line.trim();
                            if (line.isEmpty()) continue;
                            
                            // 尝试解析ID
                            try {
                                Long id = Long.parseLong(line);
                                // 查找对应的活动
                                for (Activity activity : activities) {
                                    if (activity.getId().equals(id) && activity.getStatus() == 1) {
                                        // 检查是否已经添加过这个活动
                                        if (!recommendedActivities.contains(activity)) {
                                            recommendedActivities.add(activity);
                                        }
                                        break;
                                    }
                                }
                            } catch (Exception e) {
                                // 忽略非数字行
                                continue;
                            }
                        }
                    } catch (Exception e) {
                        log.error("解析AI推荐内容失败: {}", e.getMessage());
                        // 如果解析失败，返回空列表
                        recommendedActivities = new ArrayList<>();
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("解析AI API响应失败: {}", e.getMessage());
                // 如果API响应解析失败，返回空列表
                recommendedActivities = new ArrayList<>();
            }
            
            // 如果没有推荐活动，返回空列表
            if (recommendedActivities.isEmpty()) {
                recommendedActivities = new ArrayList<>();
            }
            
            AIResponse aiResponse = new AIResponse();
            aiResponse.setRecommendedActivities(recommendedActivities);
            return aiResponse;
        } catch (Exception e) {
            log.error("AI推荐服务异常: {}", e.getMessage());
            // 处理错误，返回空列表
            AIResponse aiResponse = new AIResponse();
            aiResponse.setRecommendedActivities(new ArrayList<>());
            return aiResponse;
        }
    }
}
