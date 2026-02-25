package com.gdmu.service.impl;

import com.gdmu.mapper.ActivityMapper;
import com.gdmu.pojo.Activity;
import com.gdmu.service.ActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class ActivityServiceImpl implements ActivityService {

    @Autowired
    private ActivityMapper activityMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Activity createActivity(Long creatorId, Integer type, String title, String description, String location, java.util.Date eventTime, Integer maxPeople) {
        log.info("创建新活动，creatorId: {}, type: {}, title: {}, location: {}, eventTime: {}, maxPeople: {}",
                creatorId, type, title, location, eventTime, maxPeople);

        try {
            // 验证 creatorId 不为 null
            if (creatorId == null) {
                throw new RuntimeException("用户ID不能为空");
            }

            Activity activity = new Activity();
            activity.setTitle(title);
            activity.setDescription(description);
            activity.setLocation(location);
            activity.setEventTime(eventTime);
            activity.setMaxPeople(maxPeople);
            activity.setCurrentPeople(0); // 初始参与人数为0
            activity.setStatus(1); // 初始状态为招募中
            activity.setType(type != null ? type : 0); // 默认类型为其他
            activity.setCreatorId(creatorId);

            int rows = activityMapper.insert(activity);
            if (rows <= 0) {
                throw new RuntimeException("创建活动失败");
            }

            log.info("活动创建成功，eventId: {}", activity.getId());
            return activity;

        } catch (Exception e) {
            log.error("创建活动失败: {}", e.getMessage());
            throw new RuntimeException("创建活动失败: " + e.getMessage());
        }
    }

    @Override
    public Activity getActivityById(Long id) {
        log.info("查询活动信息，eventId: {}", id);
        return activityMapper.selectById(id);
    }

    @Override
    public List<Activity> getAllActivities() {
        log.info("查询所有活动");
        return activityMapper.selectAll();
    }

    @Override
    public List<Activity> getActivitiesByType(Integer type) {
        log.info("查询活动类型: {}", type);
        return activityMapper.selectByType(type);
    }

    @Override
    public List<Activity> getActivitiesByCreatorId(Long creatorId) {
        log.info("查询用户创建的活动，creatorId: {}", creatorId);
        return activityMapper.selectByCreatorId(creatorId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateActivity(Long eventId, String title, String description, String location, java.util.Date eventTime, Integer maxPeople, Integer type, Integer status) {
        log.info("更新活动信息，eventId: {}", eventId);

        try {
            Activity activity = activityMapper.selectById(eventId);
            if (activity == null) {
                throw new RuntimeException("活动不存在");
            }

            if (title != null) {
                activity.setTitle(title);
            }
            if (description != null) {
                activity.setDescription(description);
            }
            if (location != null) {
                activity.setLocation(location);
            }
            if (eventTime != null) {
                activity.setEventTime(eventTime);
            }
            if (maxPeople != null) {
                activity.setMaxPeople(maxPeople);
            }
            if (type != null) {
                activity.setType(type);
            }
            if (status != null) {
                activity.setStatus(status);
            }

            int rows = activityMapper.update(activity);
            if (rows <= 0) {
                throw new RuntimeException("更新活动失败");
            }

            log.info("活动更新成功，eventId: {}", eventId);

        } catch (Exception e) {
            log.error("更新活动失败: {}", e.getMessage());
            throw new RuntimeException("更新活动失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteActivity(Long eventId) {
        log.info("删除活动，eventId: {}", eventId);

        try {
            Activity activity = activityMapper.selectById(eventId);
            if (activity == null) {
                throw new RuntimeException("活动不存在");
            }

            int rows = activityMapper.deleteById(eventId);
            if (rows <= 0) {
                throw new RuntimeException("删除活动失败");
            }

            log.info("活动删除成功，eventId: {}", eventId);

        } catch (Exception e) {
            log.error("删除活动失败: {}", e.getMessage());
            throw new RuntimeException("删除活动失败: " + e.getMessage());
        }
    }
}