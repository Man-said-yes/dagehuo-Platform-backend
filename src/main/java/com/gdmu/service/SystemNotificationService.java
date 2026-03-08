package com.gdmu.service;

import com.gdmu.pojo.SystemNotification;

import java.util.List;

/**
 * 系统通知服务接口
 */
public interface SystemNotificationService {
    /**
     * 创建系统通知
     */
    void createNotification(Long userId, Long activityId, Integer notificationType, String title, String content);

    /**
     * 批量创建系统通知
     */
    void createBatchNotifications(List<Long> userIds, Long activityId, Integer notificationType, String title, String content);

    /**
     * 获取用户的系统通知列表
     */
    List<SystemNotification> getUserNotifications(Long userId);

    /**
     * 根据通知类型获取用户的系统通知列表
     */
    List<SystemNotification> getUserNotificationsByType(Long userId, Integer notificationType);

    /**
     * 获取用户的未读通知数量
     */
    int getUnreadNotificationCount(Long userId);

    /**
     * 标记通知为已读
     */
    void markAsRead(Long notificationId);

    /**
     * 批量标记通知为已读
     */
    void markMultipleAsRead(Long userId, List<Long> notificationIds);

    /**
     * 发送活动创建通知
     */
    void sendActivityCreateNotification(Long activityId, Long creatorId, String activityTitle);

    /**
     * 发送活动结束通知
     */
    void sendActivityEndNotification(Long activityId, String activityTitle);

    /**
     * 发送活动取消通知
     */
    void sendActivityCancelNotification(Long activityId, String activityTitle);

    /**
     * 发送新成员加入通知
     */
    void sendNewMemberJoinNotification(Long activityId, String activityTitle, Long newMemberId, String newMemberName);

    /**
     * 发送活动即将开始通知
     */
    void sendActivityStartingNotification(Long activityId, String activityTitle);
}