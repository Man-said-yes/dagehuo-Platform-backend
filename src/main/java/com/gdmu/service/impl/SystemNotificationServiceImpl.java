package com.gdmu.service.impl;

import com.gdmu.mapper.SystemNotificationMapper;
import com.gdmu.mapper.ParticipantMapper;
import com.gdmu.pojo.SystemNotification;
import com.gdmu.service.SystemNotificationService;
import com.gdmu.util.RedisChatUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 系统通知服务实现
 */
@Slf4j
@Service
public class SystemNotificationServiceImpl implements SystemNotificationService {

    @Autowired
    private SystemNotificationMapper systemNotificationMapper;

    @Autowired
    private ParticipantMapper participantMapper;

    @Autowired
    private RedisChatUtil redisChatUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createNotification(Long userId, Long activityId, Integer notificationType, String title, String content) {
        log.info("创建系统通知，userId: {}, activityId: {}, type: {}, title: {}", userId, activityId, notificationType, title);

        try {
            // 创建通知记录
            SystemNotification notification = new SystemNotification();
            notification.setUserId(userId);
            notification.setActivityId(activityId);
            notification.setNotificationType(notificationType);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setIsRead(0); // 默认未读

            int rows = systemNotificationMapper.insert(notification);
            if (rows <= 0) {
                throw new RuntimeException("创建通知失败");
            }

            // 通过WebSocket推送通知给在线用户
            pushNotificationToUser(userId, notification);

            log.info("系统通知创建成功，notificationId: {}", notification.getId());

        } catch (Exception e) {
            log.error("创建系统通知失败: {}", e.getMessage());
            throw new RuntimeException("创建通知失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createBatchNotifications(List<Long> userIds, Long activityId, Integer notificationType, String title, String content) {
        log.info("批量创建系统通知，userCount: {}, activityId: {}, type: {}, title: {}", userIds.size(), activityId, notificationType, title);

        try {
            for (Long userId : userIds) {
                createNotification(userId, activityId, notificationType, title, content);
            }

        } catch (Exception e) {
            log.error("批量创建系统通知失败: {}", e.getMessage());
            throw new RuntimeException("批量创建通知失败: " + e.getMessage());
        }
    }

    @Override
    public List<SystemNotification> getUserNotifications(Long userId) {
        log.info("获取用户的系统通知列表，userId: {}", userId);
        return systemNotificationMapper.selectByUserId(userId);
    }

    @Override
    public List<SystemNotification> getUserNotificationsByType(Long userId, Integer notificationType) {
        log.info("根据通知类型获取用户的系统通知列表，userId: {}, type: {}", userId, notificationType);
        return systemNotificationMapper.selectByUserIdAndType(userId, notificationType);
    }

    @Override
    public int getUnreadNotificationCount(Long userId) {
        log.info("获取用户的未读通知数量，userId: {}", userId);
        return systemNotificationMapper.countUnreadByUserId(userId);
    }

    @Override
    public void markAsRead(Long notificationId) {
        log.info("标记通知为已读，notificationId: {}", notificationId);
        systemNotificationMapper.markAsRead(notificationId);
    }

    @Override
    public void markMultipleAsRead(Long userId, List<Long> notificationIds) {
        log.info("批量标记通知为已读，userId: {}, count: {}", userId, notificationIds.size());
        systemNotificationMapper.markMultipleAsRead(userId, notificationIds);
    }

    @Override
    public void sendActivityCreateNotification(Long activityId, Long creatorId, String activityTitle) {
        log.info("发送活动创建通知，activityId: {}, creatorId: {}, title: {}", activityId, creatorId, activityTitle);

        String title = "活动发布成功";
        String content = "你发布的「" + activityTitle + "」已成功创建";
        createNotification(creatorId, activityId, 1, title, content);
    }

    @Override
    public void sendActivityEndNotification(Long activityId, String activityTitle) {
        log.info("发送活动结束通知，activityId: {}, title: {}", activityId, activityTitle);

        // 获取活动的所有参与者
        List<Long> participantIds = participantMapper.selectParticipantIdsByActivityId(activityId);
        if (participantIds.isEmpty()) {
            return;
        }

        String title = "活动已结束";
        String content = "「" + activityTitle + "」活动已结束，快去评价队友吧";
        createBatchNotifications(participantIds, activityId, 2, title, content);
    }

    @Override
    public void sendActivityCancelNotification(Long activityId, String activityTitle) {
        log.info("发送活动取消通知，activityId: {}, title: {}", activityId, activityTitle);

        // 获取活动的所有参与者
        List<Long> participantIds = participantMapper.selectParticipantIdsByActivityId(activityId);
        if (participantIds.isEmpty()) {
            return;
        }

        String title = "活动已取消";
        String content = "「" + activityTitle + "」活动已被取消";
        createBatchNotifications(participantIds, activityId, 3, title, content);
    }

    @Override
    public void sendNewMemberJoinNotification(Long activityId, String activityTitle, Long newMemberId, String newMemberName) {
        log.info("发送新成员加入通知，activityId: {}, title: {}, newMemberId: {}, newMemberName: {}", activityId, activityTitle, newMemberId, newMemberName);

        // 获取活动的所有参与者（除了新加入的成员）
        List<Long> participantIds = participantMapper.selectParticipantIdsByActivityId(activityId);
        participantIds.remove(newMemberId); // 移除新成员自己
        if (participantIds.isEmpty()) {
            return;
        }

        String title = "新成员加入";
        String content = newMemberName + "加入了你的「" + activityTitle + "」活动";
        createBatchNotifications(participantIds, activityId, 4, title, content);
    }

    @Override
    public void sendActivityStartingNotification(Long activityId, String activityTitle) {
        log.info("发送活动即将开始通知，activityId: {}, title: {}", activityId, activityTitle);

        // 获取活动的所有参与者
        List<Long> participantIds = participantMapper.selectParticipantIdsByActivityId(activityId);
        if (participantIds.isEmpty()) {
            return;
        }

        String title = "活动即将开始";
        String content = "你参与的「" + activityTitle + "」将在1小时后开始";
        createBatchNotifications(participantIds, activityId, 5, title, content);
    }

    /**
     * 通过WebSocket推送通知给用户
     */
    private void pushNotificationToUser(Long userId, SystemNotification notification) {
        try {
            // 构建通知消息
            java.util.Map<String, Object> notificationMessage = new java.util.HashMap<>();
            notificationMessage.put("type", "system_notification");
            notificationMessage.put("notificationId", notification.getId());
            notificationMessage.put("title", notification.getTitle());
            notificationMessage.put("content", notification.getContent());
            notificationMessage.put("notificationType", notification.getNotificationType());
            notificationMessage.put("createTime", notification.getCreateTime());

            // 通过Redis发布通知消息
            redisChatUtil.publishNotification(userId, notificationMessage);
            log.info("通过WebSocket推送通知给用户，userId: {}", userId);

        } catch (Exception e) {
            log.warn("推送通知失败: {}", e.getMessage());
            // 推送失败不影响通知创建
        }
    }
}