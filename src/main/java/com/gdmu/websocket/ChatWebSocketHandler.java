package com.gdmu.websocket;

import com.gdmu.mapper.ChatGroupMapper;
import com.gdmu.pojo.ChatGroup;
import com.gdmu.pojo.ChatMessage;
import com.gdmu.service.ChatService;
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

    @Autowired
    private ChatService chatService;

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
            log.info("用户会话已存储，当前在线用户数: {}", userSessions.size());

            // 获取用户所在的群
            List<ChatGroup> groups = chatGroupMapper.selectByUserId(userId);
            log.info("用户{}所在的群列表: {}", userId, groups != null ? groups.size() : 0);
            if (groups != null) {
                for (ChatGroup group : groups) {
                    log.info("  - 群ID: {}, 群名称: {}", group.getId(), group.getName());
                    // 添加用户到群在线列表
                    redisChatUtil.addOnlineUser(group.getId(), userId);
                    log.info("用户{}已添加到群{}的在线列表", userId, group.getId());
                    // 广播用户上线消息
                    broadcastUserStatus(group.getId(), userId, true);
                }
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
                
                log.info("解析消息成功，groupId: {}, content: {}", groupId, content);
                
                // 先调用ChatService保存消息到数据库
                ChatMessage chatMessage = chatService.sendMessage(groupId, userId, content, 1);
                log.info("消息已保存到数据库，messageId: {}", chatMessage.getId());
                
                // 使用JSON库构建广播消息，避免特殊字符问题
                org.json.JSONObject broadcastJson = new org.json.JSONObject();
                broadcastJson.put("type", "chat_message");
                broadcastJson.put("messageId", chatMessage.getId());
                broadcastJson.put("groupId", groupId);
                broadcastJson.put("userId", userId);
                broadcastJson.put("content", content);
                // 使用ISO 8601格式的时间字符串
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                String formattedTime = sdf.format(chatMessage.getSendTime());
                broadcastJson.put("sendTime", formattedTime);
                String broadcastMessage = broadcastJson.toString();
                
                log.info("准备广播消息: {}", broadcastMessage);
                
                // 广播消息给群内所有在线用户
                broadcastMessageToGroup(groupId, broadcastMessage);
                
                log.info("消息广播完成");
                
            } catch (Exception e) {
                log.error("处理WebSocket消息失败: {}", e.getMessage());
                e.printStackTrace();
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
    public void broadcastUserStatus(Long groupId, Long userId, boolean isOnline) {
        try {
            // 使用JSON库构建消息，避免特殊字符问题
            org.json.JSONObject statusJson = new org.json.JSONObject();
            statusJson.put("type", "user_status");
            statusJson.put("groupId", groupId);
            statusJson.put("userId", userId);
            statusJson.put("isOnline", isOnline);
            String message = statusJson.toString();

            log.info("广播用户状态，groupId: {}, userId: {}, isOnline: {}, message: {}", groupId, userId, isOnline, message);

            // 获取群在线用户
            List<Long> onlineUsers = new ArrayList<>(redisChatUtil.getOnlineUsers(groupId));
            log.info("群在线用户列表: {}", onlineUsers);
            
            for (Long onlineUserId : onlineUsers) {
                WebSocketSession session = userSessions.get(onlineUserId);
                if (session != null && session.isOpen()) {
                    log.info("发送用户状态消息给用户: {}", onlineUserId);
                    session.sendMessage(new TextMessage(message));
                } else {
                    log.warn("用户{}的会话不存在或已关闭", onlineUserId);
                }
            }
        } catch (IOException e) {
            log.error("广播用户状态失败: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    // 检查用户是否在线（有活跃的WebSocket连接）
    public boolean isUserOnline(Long userId) {
        WebSocketSession session = userSessions.get(userId);
        return session != null && session.isOpen();
    }

    // 发送消息给指定用户
    public void sendMessageToUser(Long userId, String message) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                log.info("发送消息给用户: {}, message: {}", userId, message);
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("发送消息失败: {}", e.getMessage());
                e.printStackTrace();
            }
        } else {
            log.warn("用户{}的会话不存在或已关闭", userId);
        }
    }

    // 广播消息给群内所有用户
    public void broadcastMessageToGroup(Long groupId, String message) {
        try {
            log.info("开始广播消息给群: {}, message: {}", groupId, message);
            log.info("当前所有在线用户会话: {}", userSessions.keySet());
            
            List<Long> onlineUsers = new ArrayList<>(redisChatUtil.getOnlineUsers(groupId));
            log.info("从Redis获取的群{}在线用户列表: {}", groupId, onlineUsers);
            
            int sentCount = 0;
            for (Long userId : onlineUsers) {
                WebSocketSession session = userSessions.get(userId);
                if (session != null && session.isOpen()) {
                    log.info("正在发送消息给用户: {}", userId);
                    session.sendMessage(new TextMessage(message));
                    sentCount++;
                    log.info("消息已成功发送给用户: {}", userId);
                } else {
                    log.warn("用户{}的会话不存在或已关闭，session: {}, isOpen: {}", 
                        userId, session != null, session != null ? session.isOpen() : false);
                }
            }
            
            log.info("消息广播完成，共发送给{}个用户", sentCount);
        } catch (Exception e) {
            log.error("广播消息失败: {}", e.getMessage(), e);
        }
    }
}