package com.gdmu.service;

import com.gdmu.pojo.Activity;

import java.util.List;

public interface ActivityService {
    /**
     * 创建活动
     */
    Activity createActivity(Long creatorId, Integer type, String title, String description, String location, java.util.Date eventTime, Integer maxPeople);

    /**
     * 根据ID查询活动
     */
    Activity getActivityById(Long id);

    /**
     * 查询所有活动（分页）
     */
    List<Activity> getAllActivities(int page, int pageSize);

    /**
     * 根据类型查询活动（分页）
     */
    List<Activity> getActivitiesByType(Integer type, int page, int pageSize);

    /**
     * 查询活动总数
     */
    int getActivityCount(Integer type);

    /**
     * 根据创建者ID查询活动
     */
    List<Activity> getActivitiesByCreatorId(Long creatorId);

    /**
     * 更新活动信息
     */
    void updateActivity(Long eventId, String title, String description, String location, java.util.Date eventTime, Integer maxPeople, Integer type, Integer status);

    /**
     * 删除活动
     */
    void deleteActivity(Long eventId);

    /**
     * 用户加入活动
     */
    void joinActivity(Long activityId, Long userId);

    /**
     * 用户退出活动
     */
    void exitActivity(Long activityId, Long userId);

    /**
     * 获取用户参与的活动
     */
    List<Activity> getActivitiesByParticipantId(Long userId);

    /**
     * 获取活动的参与者列表
     */
    List<Long> getActivityParticipants(Long activityId);
}