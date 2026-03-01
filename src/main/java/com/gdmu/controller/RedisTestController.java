package com.gdmu.controller;

import com.gdmu.util.RedisChatUtil;
import com.gdmu.pojo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/redis")
@CrossOrigin(origins = "*")
@Tag(name = "Redis测试", description = "Redis功能测试接口")
public class RedisTestController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisChatUtil redisChatUtil;

    @Operation(summary = "测试Redis连接", description = "测试Redis是否正常连接")
    @GetMapping("/test")
    public Result testRedisConnection() {
        try {
            System.out.println("开始测试Redis连接...");
            // 测试Redis连接
            stringRedisTemplate.opsForValue().set("test_key", "test_value");
            System.out.println("设置测试键值对成功");
            String value = stringRedisTemplate.opsForValue().get("test_key");
            System.out.println("获取测试值: " + value);
            stringRedisTemplate.delete("test_key");
            System.out.println("删除测试键值对成功");

            if ("test_value".equals(value)) {
                System.out.println("Redis连接正常");
                return Result.success("Redis连接正常");
            } else {
                System.out.println("Redis连接异常，获取的值: " + value);
                return Result.error("Redis连接异常");
            }
        } catch (Exception e) {
            System.out.println("Redis连接失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error("Redis连接失败: " + e.getMessage());
        }
    }

    @Operation(summary = "测试未读消息数", description = "测试未读消息数的Redis存储")
    @PostMapping("/unread")
    public Result testUnreadCount(@RequestParam Long userId, @RequestParam Long groupId) {
        try {
            // 增加未读消息数
            redisChatUtil.incrementUnreadCount(userId, groupId);
            redisChatUtil.incrementUnreadCount(userId, groupId);
            
            // 获取未读消息数
            Long unreadCount = redisChatUtil.getUnreadCount(userId, groupId);
            
            // 获取用户所有群的未读消息数
            Map<Long, Long> unreadCounts = redisChatUtil.getUnreadCounts(userId);
            
            // 重置未读消息数
            redisChatUtil.resetUnreadCount(userId, groupId);
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("unreadCount", unreadCount);
            result.put("unreadCounts", unreadCounts);
            result.put("afterReset", redisChatUtil.getUnreadCount(userId, groupId));
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("测试未读消息数失败: " + e.getMessage());
        }
    }

    @Operation(summary = "测试在线用户列表", description = "测试在线用户列表的Redis存储")
    @PostMapping("/online")
    public Result testOnlineUsers(@RequestParam Long groupId, @RequestParam Long userId1, @RequestParam Long userId2) {
        try {
            // 添加在线用户
            redisChatUtil.addOnlineUser(groupId, userId1);
            redisChatUtil.addOnlineUser(groupId, userId2);
            
            // 获取在线用户列表
            Set<Long> onlineUsers = redisChatUtil.getOnlineUsers(groupId);
            
            // 检查用户是否在线
            boolean isUser1Online = redisChatUtil.isUserOnline(groupId, userId1);
            boolean isUser2Online = redisChatUtil.isUserOnline(groupId, userId2);
            
            // 移除在线用户
            redisChatUtil.removeOnlineUser(groupId, userId1);
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("onlineUsers", onlineUsers);
            result.put("isUser1Online", isUser1Online);
            result.put("isUser2Online", isUser2Online);
            result.put("afterRemove", redisChatUtil.getOnlineUsers(groupId));
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("测试在线用户列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "测试WebSocket会话管理", description = "测试WebSocket会话的Redis存储")
    @PostMapping("/session")
    public Result testSessionManagement(@RequestParam Long userId, @RequestParam String sessionId) {
        try {
            // 存储会话
            redisChatUtil.storeSession(userId, sessionId);
            
            // 获取会话
            String storedSessionId = redisChatUtil.getSessionId(userId);
            
            // 获取所有会话
            Map<Long, String> sessionMap = redisChatUtil.getSessionMap();
            
            // 移除会话
            redisChatUtil.removeSession(userId);
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("storedSessionId", storedSessionId);
            result.put("sessionMap", sessionMap);
            result.put("afterRemove", redisChatUtil.getSessionId(userId));
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("测试会话管理失败: " + e.getMessage());
        }
    }
}