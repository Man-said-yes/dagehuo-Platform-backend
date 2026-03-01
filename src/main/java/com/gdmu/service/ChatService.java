package com.gdmu.service;

import com.gdmu.pojo.ChatGroup;
import com.gdmu.pojo.ChatMessage;

import java.util.List;
import java.util.Map;

public interface ChatService {
    ChatGroup createGroup(Long activityId, String groupName);
    ChatGroup getGroupById(Long groupId);
    ChatGroup getGroupByActivityId(Long activityId);
    List<Map<String, Object>> getUserGroups(Long userId);
    List<Map<String, Object>> getGroupMembers(Long groupId);
    ChatMessage sendMessage(Long groupId, Long userId, String content, Integer type);
    List<Map<String, Object>> getGroupMessages(Long groupId, int page, int pageSize);
    void markMessageAsRead(Long groupId, Long userId, Long lastReadMessageId);
    void addGroupMember(Long groupId, Long userId);
    void removeGroupMember(Long groupId, Long userId);
}
