package com.gdmu.service;

import com.gdmu.pojo.MealEvent;

import java.util.List;

public interface MealEventService {
    /**
     * 创建新活动
     * @param creatorId 创建者ID
     * @param type 活动类型
     * @param title 活动标题
     * @param description 活动描述
     * @param location 活动地点
     * @param eventTime 活动时间
     * @param maxPeople 最大参与人数
     * @return 创建的活动
     */
    MealEvent createEvent(Long creatorId, Integer type, String title, String description, String location, java.util.Date eventTime, Integer maxPeople);

    /**
     * 根据ID获取活动
     * @param id 活动ID
     * @return 活动信息
     */
    MealEvent getEventById(Long id);

    /**
     * 获取所有活动
     * @return 活动列表
     */
    List<MealEvent> getAllEvents();

    /**
     * 根据类型获取活动
     * @param type 活动类型
     * @return 活动列表
     */
    List<MealEvent> getEventsByType(Integer type);

    /**
     * 获取用户创建的活动
     * @param creatorId 创建者ID
     * @return 活动列表
     */
    List<MealEvent> getEventsByCreatorId(Long creatorId);

    /**
     * 更新活动信息
     * @param eventId 活动ID
     * @param title 活动标题
     * @param description 活动描述
     * @param location 活动地点
     * @param eventTime 活动时间
     * @param maxPeople 最大参与人数
     * @param type 活动类型
     * @param status 活动状态
     */
    void updateEvent(Long eventId, String title, String description, String location, java.util.Date eventTime, Integer maxPeople, Integer type, Integer status);

    /**
     * 删除活动
     * @param eventId 活动ID
     */
    void deleteEvent(Long eventId);
}