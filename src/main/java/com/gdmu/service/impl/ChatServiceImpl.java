package com.gdmu.service.impl;

import com.gdmu.mapper.ChatGroupMapper;
import com.gdmu.mapper.ChatGroupMemberMapper;
import com.gdmu.mapper.ChatMessageMapper;
import com.gdmu.mapper.UserMapper;
import com.gdmu.pojo.ChatGroup;
import com.gdmu.pojo.ChatGroupMember;
import com.gdmu.pojo.ChatMessage;
import com.gdmu.pojo.User;
import com.gdmu.service.ChatService;
import com.gdmu.util.RedisChatUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatGroupMapper chatGroupMapper;

    @Autowired
    private ChatGroupMemberMapper chatGroupMemberMapper;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisChatUtil redisChatUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatGroup createGroup(Long activityId, String groupName) {
        log.info("创建群聊，activityId: {}, groupName: {}", activityId, groupName);
        
        ChatGroup chatGroup = new ChatGroup();
        chatGroup.setName(groupName);
        chatGroup.setActivityId(activityId);
        
        int rows = chatGroupMapper.insert(chatGroup);
        if (rows <= 0) {
            throw new RuntimeException("创建群聊失败");
        }
        
        return chatGroup;
    }

    @Override
    public ChatGroup getGroupById(Long groupId) {
        return chatGroupMapper.selectById(groupId);
    }

    @Override
    public ChatGroup getGroupByActivityId(Long activityId) {
        return chatGroupMapper.selectByActivityId(activityId);
    }

    @Override
    public List<Map<String, Object>> getUserGroups(Long userId) {
        log.info("获取用户群聊列表，userId: {}", userId);

        List<ChatGroup> groups = chatGroupMapper.selectByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();

        // 获取用户所有群的未读消息数
        Map<Long, Long> unreadCounts = redisChatUtil.getUnreadCounts(userId);

        for (ChatGroup group : groups) {
            Map<String, Object> groupInfo = new HashMap<>();
            groupInfo.put("groupId", group.getId());
            groupInfo.put("groupName", group.getName());
            groupInfo.put("activityId", group.getActivityId());

            ChatMessage lastMessage = chatMessageMapper.selectLastMessageByGroupId(group.getId());
            if (lastMessage != null) {
                groupInfo.put("lastMessage", lastMessage.getContent());
                groupInfo.put("lastMessageTime", lastMessage.getSendTime());
            }

            // 从Redis获取未读消息数
            Long unreadCount = unreadCounts.getOrDefault(group.getId(), 0L);
            groupInfo.put("unreadCount", unreadCount);

            result.add(groupInfo);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getGroupMembers(Long groupId) {
        log.info("获取群聊成员列表，groupId: {}", groupId);
        
        List<ChatGroupMember> members = chatGroupMemberMapper.selectByGroupId(groupId);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (ChatGroupMember member : members) {
            Map<String, Object> memberInfo = new HashMap<>();
            User user = userMapper.selectById(member.getUserId());
            if (user != null) {
                memberInfo.put("userId", user.getId());
                memberInfo.put("nickname", user.getNickname());
                memberInfo.put("avatar", user.getAvatar());
            }
            result.add(memberInfo);
        }
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatMessage sendMessage(Long groupId, Long userId, String content, Integer type) {
        log.info("发送消息，groupId: {}, userId: {}, content: {}", groupId, userId, content);

        ChatMessage message = new ChatMessage();
        message.setGroupId(groupId);
        message.setUserId(userId);
        message.setContent(content);
        message.setType(type != null ? type : 1);

        int rows = chatMessageMapper.insert(message);
        if (rows <= 0) {
            throw new RuntimeException("发送消息失败");
        }

        // 增加其他群成员的未读消息数
        List<ChatGroupMember> members = chatGroupMemberMapper.selectByGroupId(groupId);
        for (ChatGroupMember member : members) {
            if (!member.getUserId().equals(userId)) {
                redisChatUtil.incrementUnreadCount(member.getUserId(), groupId);
            }
        }

        return message;
    }

    @Override
    public List<Map<String, Object>> getGroupMessages(Long groupId, int page, int pageSize) {
        log.info("获取群聊消息，groupId: {}, page: {}, pageSize: {}", groupId, page, pageSize);
        
        int offset = (page - 1) * pageSize;
        List<ChatMessage> messages = chatMessageMapper.selectByGroupId(groupId, offset, pageSize);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (ChatMessage message : messages) {
            Map<String, Object> messageInfo = new HashMap<>();
            messageInfo.put("messageId", message.getId());
            messageInfo.put("groupId", message.getGroupId());
            messageInfo.put("userId", message.getUserId());
            messageInfo.put("content", message.getContent());
            messageInfo.put("type", message.getType());
            messageInfo.put("sendTime", message.getSendTime());
            
            if (message.getUserId() != null) {
                User user = userMapper.selectById(message.getUserId());
                if (user != null) {
                    messageInfo.put("nickname", user.getNickname());
                    messageInfo.put("avatar", user.getAvatar());
                }
            }
            
            result.add(messageInfo);
        }
        
        return result;
    }

    @Override
    public void markMessageAsRead(Long groupId, Long userId, Long lastReadMessageId) {
        log.info("标记消息已读，groupId: {}, userId: {}, lastReadMessageId: {}", groupId, userId, lastReadMessageId);

        chatGroupMemberMapper.updateLastReadMessageId(groupId, userId, lastReadMessageId);
        // 重置Redis中的未读消息数
        redisChatUtil.resetUnreadCount(userId, groupId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addGroupMember(Long groupId, Long userId) {
        log.info("添加群成员，groupId: {}, userId: {}", groupId, userId);
        
        ChatGroupMember existingMember = chatGroupMemberMapper.selectByGroupIdAndUserId(groupId, userId);
        if (existingMember == null) {
            ChatGroupMember member = new ChatGroupMember();
            member.setGroupId(groupId);
            member.setUserId(userId);
            
            int rows = chatGroupMemberMapper.insert(member);
            if (rows <= 0) {
                throw new RuntimeException("添加群成员失败");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeGroupMember(Long groupId, Long userId) {
        log.info("移除群成员，groupId: {}, userId: {}", groupId, userId);
        
        int rows = chatGroupMemberMapper.deleteByGroupIdAndUserId(groupId, userId);
        if (rows <= 0) {
            throw new RuntimeException("移除群成员失败");
        }
    }
}
