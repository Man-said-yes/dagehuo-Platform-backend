package com.gdmu.service;

import com.gdmu.pojo.Activity;

import java.util.List;

public interface ActivityService {
    /**
     * 创建活动
     */
    Activity createActivity(Long creatorId, Integer type, String title, String description, String location, String campus, Double longitude, Double latitude, java.util.Date eventTime, Integer maxPeople);

    /**
     * 根据ID查询活动
     */
    Activity getActivityById(Long id);

    /**
     * 查询所有活动（分页，支持排序）
     */
    List<Activity> getAllActivities(int page, int pageSize, String sortBy, String order);

    /**
     * 根据类型查询活动（分页，支持排序）
     */
    List<Activity> getActivitiesByType(Integer type, int page, int pageSize, String sortBy, String order);

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
    void updateActivity(Long eventId, String title, String description, String location, String campus, Double longitude, Double latitude, java.util.Date eventTime, Integer maxPeople, Integer type, Integer status);

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

    /**
     * 获取活动的参与者详细信息列表
     */
    List<com.gdmu.pojo.User> getActivityParticipantsInfo(Long activityId);

    /**
     * 更新活动状态
     */
    void updateActivityStatus(Long activityId, Integer status);
    
    /**
     * 根据距离查询活动（由近及远）
     */
    List<Activity> getActivitiesByDistanceAsc(Double longitude, Double latitude, Integer type, int page, int pageSize);
    
    /**
     * 根据距离查询活动（由远及近）
     */
    List<Activity> getActivitiesByDistanceDesc(Double longitude, Double latitude, Integer type, int page, int pageSize);
    
    /**
     * 查询符合距离条件的活动总数
     */
    int getActivityCountByDistance(Integer type);

    /**
     * 举报活动
     */
    void reportActivity(Long activityId, Long reporterUserId, String reportReason);

    /**
     * 获取被举报活动列表（分页）
     */
    List<com.gdmu.pojo.ActivityReport> getReportedActivities(int page, int pageSize);

    /**
     * 获取被举报活动总数
     */
    int getReportedActivityCount();

    /**
     * 根据处理状态获取被举报活动列表（分页）
     */
    List<com.gdmu.pojo.ActivityReport> getReportedActivitiesByStatus(Integer handleStatus, int page, int pageSize);

    /**
     * 根据处理状态获取被举报活动总数
     */
    int getReportedActivityCountByStatus(Integer handleStatus);
    
    /**
     * 核实举报并结束活动，通知所有参与用户
     */
    void verifyReport(Long reportId);
}