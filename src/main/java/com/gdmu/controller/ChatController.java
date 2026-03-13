package com.gdmu.controller;

import com.gdmu.mapper.UserMapper;
import com.gdmu.pojo.ChatGroup;
import com.gdmu.pojo.ChatMessage;
import com.gdmu.pojo.Result;
import com.gdmu.pojo.User;
import com.gdmu.service.ChatService;
import com.gdmu.util.RedisChatUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@Validated
@CrossOrigin(origins = "*")
@Tag(name = "聊天管理", description = "群聊相关接口")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private RedisChatUtil redisChatUtil;

    @Autowired
    private UserMapper userMapper;

    @Operation(summary = "获取用户群聊列表", description = "获取当前用户的所有群聊")
    @GetMapping("/groups")
    public Result getUserGroups(jakarta.servlet.http.HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            List<Map<String, Object>> groups = chatService.getUserGroups(userId);
            return Result.success(groups);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取群聊详情", description = "根据群聊ID获取群聊详细信息")
    @GetMapping("/group/{groupId}")
    public Result getGroupDetail(@PathVariable Long groupId) {
        try {
            ChatGroup group = chatService.getGroupById(groupId);
            if (group == null) {
                return Result.error("群聊不存在");
            }
            
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("id", group.getId());
            result.put("name", group.getName());
            result.put("type", group.getType());
            result.put("activityId", group.getActivityId());
            result.put("status", group.getStatus());
            result.put("createTime", group.getCreateTime());
            
            if (group.getOwnerId() != null) {
                User owner = userMapper.selectById(group.getOwnerId());
                if (owner != null) {
                    java.util.Map<String, Object> ownerInfo = new java.util.HashMap<>();
                    ownerInfo.put("userId", owner.getId());
                    ownerInfo.put("nickname", owner.getNickname());
                    ownerInfo.put("avatar", owner.getAvatar());
                    result.put("owner", ownerInfo);
                }
            }
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取群聊成员列表", description = "获取指定群聊的成员列表")
    @GetMapping("/group/{groupId}/members")
    public Result getGroupMembers(@PathVariable Long groupId) {
        try {
            List<Map<String, Object>> members = chatService.getGroupMembers(groupId);
            return Result.success(members);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取群聊消息", description = "获取指定群聊的消息记录，支持分页")
    @GetMapping("/group/{groupId}/messages")
    public Result getGroupMessages(
            @PathVariable Long groupId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        try {
            if (page < 1) page = 1;
            if (pageSize < 1 || pageSize > 100) pageSize = 20;
            
            List<Map<String, Object>> messages = chatService.getGroupMessages(groupId, page, pageSize);
            return Result.success(messages);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "发送消息", description = "在指定群聊中发送消息")
    @PostMapping("/group/{groupId}/message")
    public Result sendMessage(
            @PathVariable Long groupId,
            @RequestBody Map<String, Object> request,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            String content = (String) request.get("content");
            Integer type = request.get("type") != null ? (Integer) request.get("type") : 1;
            
            if (content == null || content.trim().isEmpty()) {
                return Result.error("消息内容不能为空");
            }
            
            ChatMessage message = chatService.sendMessage(groupId, userId, content, type);
            return Result.success(message);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "标记消息已读", description = "标记群聊消息为已读")
    @PostMapping("/group/{groupId}/read")
    public Result markMessageAsRead(
            @PathVariable Long groupId,
            @RequestBody Map<String, Object> request,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            Long lastReadMessageId = Long.valueOf(request.get("lastReadMessageId").toString());
            
            chatService.markMessageAsRead(groupId, userId, lastReadMessageId);
            return Result.success("标记已读成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "添加群成员", description = "向指定群聊添加成员")
    @PostMapping("/group/{groupId}/member")
    public Result addGroupMember(
            @PathVariable Long groupId,
            @RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            chatService.addGroupMember(groupId, userId);
            return Result.success("添加成员成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "移除群成员", description = "从指定群聊移除成员")
    @DeleteMapping("/group/{groupId}/member/{userId}")
    public Result removeGroupMember(
            @PathVariable Long groupId,
            @PathVariable Long userId) {
        try {
            chatService.removeGroupMember(groupId, userId);
            return Result.success("移除成员成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取群在线用户列表", description = "获取指定群的在线用户列表")
    @GetMapping("/group/{groupId}/online")
    public Result getGroupOnlineUsers(@PathVariable Long groupId) {
        try {
            java.util.Set<Long> onlineUserIds = redisChatUtil.getOnlineUsers(groupId);
            // 查询用户信息
            java.util.List<java.util.Map<String, Object>> onlineUsers = new java.util.ArrayList<>();
            for (Long userId : onlineUserIds) {
                User user = userMapper.selectById(userId);
                if (user != null) {
                    java.util.Map<String, Object> userInfo = new java.util.HashMap<>();
                    userInfo.put("userId", user.getId());
                    userInfo.put("nickname", user.getNickname());
                    userInfo.put("avatar", user.getAvatar());
                    onlineUsers.add(userInfo);
                }
            }
            return Result.success(onlineUsers);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @Operation(summary = "创建兴趣群", description = "活动结束后创建兴趣群（需要大于2人）")
    @PostMapping("/interest-group")
    public Result createInterestGroup(@RequestBody Map<String, Object> request) {
        try {
            Long activityId = Long.valueOf(request.get("activityId").toString());
            String groupName = (String) request.get("groupName");
            Long ownerId = Long.valueOf(request.get("ownerId").toString());
            
            if (groupName == null || groupName.trim().isEmpty()) {
                return Result.error("群聊名称不能为空");
            }
            
            ChatGroup chatGroup = chatService.createInterestGroup(activityId, groupName, ownerId);
            return Result.success(chatGroup);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @Operation(summary = "解散群聊", description = "将群聊状态设置为解散")
    @PostMapping("/group/{groupId}/dissolve")
    public Result dissolveGroup(@PathVariable Long groupId) {
        try {
            chatService.dissolveGroup(groupId);
            return Result.success("群聊已解散");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
