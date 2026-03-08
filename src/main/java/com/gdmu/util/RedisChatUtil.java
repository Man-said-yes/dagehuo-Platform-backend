package com.gdmu.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class RedisChatUtil {
    @Autowired
    public StringRedisTemplate stringRedisTemplate;

    // ===== 未读消息数相关 =====

    // 增加未读消息数
    public void incrementUnreadCount(Long userId, Long groupId) {
        String key = "unread:" + userId;
        String field = groupId.toString();
        Object value = stringRedisTemplate.opsForHash().get(key, field);
        long count = value != null ? Long.parseLong(value.toString()) : 0;
        stringRedisTemplate.opsForHash().put(key, field, String.valueOf(count + 1));
    }

    // 获取未读消息数
    public Long getUnreadCount(Long userId, Long groupId) {
        String key = "unread:" + userId;
        String field = groupId.toString();
        Object value = stringRedisTemplate.opsForHash().get(key, field);
        return value != null ? Long.parseLong(value.toString()) : 0;
    }

    // 重置未读消息数
    public void resetUnreadCount(Long userId, Long groupId) {
        String key = "unread:" + userId;
        String field = groupId.toString();
        stringRedisTemplate.opsForHash().delete(key, field);
    }

    // 获取用户所有群的未读消息数
    public Map<Long, Long> getUnreadCounts(Long userId) {
        String key = "unread:" + userId;
        Map<Object, Object> hash = stringRedisTemplate.opsForHash().entries(key);
        Map<Long, Long> result = new HashMap<>();
        for (Map.Entry<Object, Object> entry : hash.entrySet()) {
            result.put(Long.valueOf(entry.getKey().toString()), Long.valueOf(entry.getValue().toString()));
        }
        return result;
    }

    // ===== 在线用户相关 =====

    // 添加在线用户
    public void addOnlineUser(Long groupId, Long userId) {
        String key = "online:" + groupId;
        stringRedisTemplate.opsForSet().add(key, userId.toString());
    }

    // 移除在线用户
    public void removeOnlineUser(Long groupId, Long userId) {
        String key = "online:" + groupId;
        stringRedisTemplate.opsForSet().remove(key, userId.toString());
    }

    // 获取群在线用户列表
    public Set<Long> getOnlineUsers(Long groupId) {
        String key = "online:" + groupId;
        Set<String> members = stringRedisTemplate.opsForSet().members(key);
        Set<Long> result = new HashSet<>();
        if (members != null) {
            for (String member : members) {
                result.add(Long.valueOf(member));
            }
        }
        return result;
    }

    // 检查用户是否在线
    public boolean isUserOnline(Long groupId, Long userId) {
        String key = "online:" + groupId;
        return stringRedisTemplate.opsForSet().isMember(key, userId.toString());
    }

    // ===== WebSocket会话管理 =====

    // 存储会话映射
    public void storeSession(Long userId, String sessionId) {
        stringRedisTemplate.opsForHash().put("ws:session", userId.toString(), sessionId);
    }

    // 获取会话ID
    public String getSessionId(Long userId) {
        Object value = stringRedisTemplate.opsForHash().get("ws:session", userId.toString());
        return value != null ? value.toString() : null;
    }

    // 移除会话映射
    public void removeSession(Long userId) {
        stringRedisTemplate.opsForHash().delete("ws:session", userId.toString());
    }

    // 获取所有会话映射
    public Map<Long, String> getSessionMap() {
        Map<Object, Object> hash = stringRedisTemplate.opsForHash().entries("ws:session");
        Map<Long, String> result = new HashMap<>();
        for (Map.Entry<Object, Object> entry : hash.entrySet()) {
            result.put(Long.valueOf(entry.getKey().toString()), entry.getValue().toString());
        }
        return result;
    }

    // ===== 系统通知相关 =====

    @Autowired
    private org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer;

    // 发布系统通知
    public void publishNotification(Long userId, Object notification) {
        try {
            // 使用Redis的发布/订阅功能发布通知
            // 频道格式：notification:{userId}
            String channel = "notification:" + userId;
            // 将notification对象序列化为JSON字符串
            byte[] messageBytes = jackson2JsonRedisSerializer.serialize(notification);
            stringRedisTemplate.convertAndSend(channel, new String(messageBytes));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}