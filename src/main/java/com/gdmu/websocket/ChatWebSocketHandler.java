package com.gdmu.websocket;

import com.gdmu.mapper.ChatGroupMapper;
import com.gdmu.pojo.ChatGroup;
import com.gdmu.util.RedisChatUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private RedisChatUtil redisChatUtil;

    @Autowired
    private ChatGroupMapper chatGroupMapper;

    // 存储会话映射
    private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从会话中获取用户ID
        Long userId = (Long) session.getAttributes().get("userId");
        log.info("WebSocket连接建立，userId: {}", userId);

        if (userId != null) {
            // 存储会话映射
            userSessions.put(userId, session);
            redisChatUtil.storeSession(userId, session.getId());

            // 获取用户所在的群
            List<ChatGroup> groups = chatGroupMapper.selectByUserId(userId);
            for (ChatGroup group : groups) {
                // 添加用户到群在线列表
                redisChatUtil.addOnlineUser(group.getId(), userId);
                // 广播用户上线消息
                broadcastUserStatus(group.getId(), userId, true);
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 从会话中获取用户ID
        Long userId = (Long) session.getAttributes().get("userId");
        log.info("收到WebSocket消息，userId: {}, content: {}", userId, message.getPayload());

        if (userId != null) {
            // 解析消息内容
            String payload = message.getPayload();
            try {
                // 假设消息格式为 JSON: {"groupId": 1, "content": "消息内容"}
                org.json.JSONObject jsonMessage = new org.json.JSONObject(payload);
                Long groupId = jsonMessage.getLong("groupId");
                String content = jsonMessage.getString("content");
                
                // 存储消息到 Redis
                // 这里可以根据需要存储更多信息，如发送时间、消息类型等
                String messageKey = "message:" + groupId + ":" + System.currentTimeMillis();
                redisChatUtil.stringRedisTemplate.opsForHash().put(messageKey, "userId", userId.toString());
                redisChatUtil.stringRedisTemplate.opsForHash().put(messageKey, "content", content);
                redisChatUtil.stringRedisTemplate.opsForHash().put(messageKey, "sendTime", String.valueOf(System.currentTimeMillis()));
                
                // 增加其他群成员的未读消息数
                List<Long> onlineUsers = new ArrayList<>(redisChatUtil.getOnlineUsers(groupId));
                for (Long onlineUserId : onlineUsers) {
                    if (!onlineUserId.equals(userId)) {
                        redisChatUtil.incrementUnreadCount(onlineUserId, groupId);
                    }
                }
                
                // 广播消息给群内所有在线用户
                String broadcastMessage = String.format("{\"type\": \"chat_message\", \"groupId\": %d, \"userId\": %d, \"content\": \"%s\", \"sendTime\": %d}", 
                        groupId, userId, content, System.currentTimeMillis());
                broadcastMessageToGroup(groupId, broadcastMessage);
                
            } catch (Exception e) {
                log.error("处理WebSocket消息失败: {}", e.getMessage());
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 从会话中获取用户ID
        Long userId = (Long) session.getAttributes().get("userId");
        log.info("WebSocket连接关闭，userId: {}", userId);

        if (userId != null) {
            // 移除会话映射
            userSessions.remove(userId);
            redisChatUtil.removeSession(userId);

            // 获取用户所在的群
            List<ChatGroup> groups = chatGroupMapper.selectByUserId(userId);
            for (ChatGroup group : groups) {
                // 从群在线列表移除用户
                redisChatUtil.removeOnlineUser(group.getId(), userId);
                // 广播用户下线消息
                broadcastUserStatus(group.getId(), userId, false);
            }
        }
    }

    // 广播用户状态变更
    private void broadcastUserStatus(Long groupId, Long userId, boolean isOnline) {
        try {
            // 构建状态消息
            String message = String.format("{\"type\": \"user_status\", \"groupId\": %d, \"userId\": %d, \"isOnline\": %b}", 
                    groupId, userId, isOnline);

            // 获取群在线用户
            List<Long> onlineUsers = new ArrayList<>(redisChatUtil.getOnlineUsers(groupId));
            for (Long onlineUserId : onlineUsers) {
                WebSocketSession session = userSessions.get(onlineUserId);
                if (session != null && session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                }
            }
        } catch (IOException e) {
            log.error("广播用户状态失败: {}", e.getMessage());
        }
    }

    // 发送消息给指定用户
    public void sendMessageToUser(Long userId, String message) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("发送消息失败: {}", e.getMessage());
            }
        }
    }

    // 广播消息给群内所有用户
    public void broadcastMessageToGroup(Long groupId, String message) {
        try {
            List<Long> onlineUsers = new ArrayList<>(redisChatUtil.getOnlineUsers(groupId));
            for (Long userId : onlineUsers) {
                WebSocketSession session = userSessions.get(userId);
                if (session != null && session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                }
            }
        } catch (IOException e) {
            log.error("广播消息失败: {}", e.getMessage());
        }
    }
}