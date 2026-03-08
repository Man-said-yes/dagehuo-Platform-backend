package com.gdmu.config;

import com.gdmu.websocket.ChatWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis系统通知监听器
 * 用于接收Redis发布的系统通知消息，并通过WebSocket推送给用户
 */
@Slf4j
@Component
public class RedisNotificationListener implements MessageListener {

    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            // 解析频道名称，获取用户ID
            String channel = new String(message.getChannel());
            if (channel.startsWith("notification:")) {
                String userIdStr = channel.substring("notification:".length());
                Long userId = Long.parseLong(userIdStr);
                
                // 解析消息内容
                String messageContent = new String(message.getBody());
                log.info("收到系统通知消息，userId: {}, content: {}", userId, messageContent);
                
                // 通过WebSocket发送消息给用户
                chatWebSocketHandler.sendMessageToUser(userId, messageContent);
            }
        } catch (Exception e) {
            log.error("处理系统通知消息失败: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}