package com.gdmu.service.impl;

import com.gdmu.mapper.ActivityMapper;
import com.gdmu.mapper.ParticipantMapper;
import com.gdmu.pojo.Activity;
import com.gdmu.pojo.Participant;
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

    @Autowired
    private ParticipantMapper participantMapper;

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
            activity.setCurrentPeople(1); // 初始参与人数为1（创建者）
            activity.setStatus(1); // 初始状态为招募中
            activity.setType(type != null ? type : 0); // 默认类型为其他
            activity.setCreatorId(creatorId);

            int rows = activityMapper.insert(activity);
            if (rows <= 0) {
                throw new RuntimeException("创建活动失败");
            }

            // 将创建者添加为参与者
            Participant participant = new Participant();
            participant.setActivityId(activity.getId());
            participant.setUserId(creatorId);
            participant.setStatus(1); // 1-已报名
            
            int participantRows = participantMapper.insert(participant);
            if (participantRows <= 0) {
                throw new RuntimeException("添加参与者失败");
            }

            log.info("活动创建成功，eventId: {}，创建者已自动加入", activity.getId());
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
    public List<Activity> getAllActivities(int page, int pageSize) {
        log.info("查询所有活动（分页），page: {}, pageSize: {}", page, pageSize);
        int offset = (page - 1) * pageSize;
        return activityMapper.selectAllWithPagination(offset, pageSize);
    }

    @Override
    public List<Activity> getActivitiesByType(Integer type, int page, int pageSize) {
        log.info("查询活动类型（分页）: {}, page: {}, pageSize: {}", type, page, pageSize);
        int offset = (page - 1) * pageSize;
        return activityMapper.selectByTypeWithPagination(type, offset, pageSize);
    }

    @Override
    public int getActivityCount(Integer type) {
        log.info("查询活动总数，type: {}", type);
        return activityMapper.countActivities(type);
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void joinActivity(Long activityId, Long userId) {
        log.info("用户加入活动，activityId: {}, userId: {}", activityId, userId);

        try {
            // 检查活动是否存在
            Activity activity = activityMapper.selectById(activityId);
            if (activity == null) {
                throw new RuntimeException("活动不存在");
            }

            // 检查活动是否已满
            if (activity.getCurrentPeople() >= activity.getMaxPeople()) {
                throw new RuntimeException("活动人数已满");
            }

            // 检查用户是否已经参与
            Participant existingParticipant = participantMapper.selectByActivityIdAndUserId(activityId, userId);
            if (existingParticipant != null) {
                throw new RuntimeException("您已参与该活动");
            }

            // 添加用户到参与者列表
            Participant participant = new Participant();
            participant.setActivityId(activityId);
            participant.setUserId(userId);
            participant.setStatus(1); // 1-已报名

            int participantRows = participantMapper.insert(participant);
            if (participantRows <= 0) {
                throw new RuntimeException("加入活动失败");
            }

            // 更新活动的当前参与人数
            int newCurrentPeople = activity.getCurrentPeople() + 1;
            activityMapper.updateCurrentPeople(activityId, newCurrentPeople);

            log.info("用户加入活动成功，activityId: {}, userId: {}", activityId, userId);

        } catch (Exception e) {
            log.error("用户加入活动失败: {}", e.getMessage());
            throw new RuntimeException("加入活动失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void exitActivity(Long activityId, Long userId) {
        log.info("用户退出活动，activityId: {}, userId: {}", activityId, userId);

        try {
            // 检查活动是否存在
            Activity activity = activityMapper.selectById(activityId);
            if (activity == null) {
                throw new RuntimeException("活动不存在");
            }

            // 检查用户是否参与了活动
            Participant existingParticipant = participantMapper.selectByActivityIdAndUserId(activityId, userId);
            if (existingParticipant == null) {
                throw new RuntimeException("您未参与该活动");
            }

            // 从参与者列表中移除用户
            int deleteRows = participantMapper.deleteByActivityIdAndUserId(activityId, userId);
            if (deleteRows <= 0) {
                throw new RuntimeException("退出活动失败");
            }

            // 更新活动的当前参与人数
            int newCurrentPeople = activity.getCurrentPeople() - 1;
            if (newCurrentPeople < 0) newCurrentPeople = 0;
            activityMapper.updateCurrentPeople(activityId, newCurrentPeople);

            log.info("用户退出活动成功，activityId: {}, userId: {}", activityId, userId);

        } catch (Exception e) {
            log.error("用户退出活动失败: {}", e.getMessage());
            throw new RuntimeException("退出活动失败: " + e.getMessage());
        }
    }

    @Override
    public List<Activity> getActivitiesByParticipantId(Long userId) {
        log.info("查询用户参与的活动，userId: {}", userId);

        try {
            // 查询用户参与的所有活动ID
            List<Participant> participants = participantMapper.selectByUserId(userId);
            if (participants.isEmpty()) {
                return java.util.Collections.emptyList();
            }

            // 提取活动ID列表
            java.util.List<Long> activityIds = participants.stream()
                    .map(Participant::getActivityId)
                    .collect(java.util.stream.Collectors.toList());

            // 查询活动详情
            java.util.List<Activity> activities = new java.util.ArrayList<>();
            for (Long activityId : activityIds) {
                Activity activity = activityMapper.selectById(activityId);
                if (activity != null) {
                    activities.add(activity);
                }
            }

            return activities;

        } catch (Exception e) {
            log.error("查询用户参与的活动失败: {}", e.getMessage());
            throw new RuntimeException("查询活动失败: " + e.getMessage());
        }
    }

    @Override
    public List<Long> getActivityParticipants(Long activityId) {
        log.info("查询活动的参与者，activityId: {}", activityId);

        try {
            // 查询活动的所有参与者
            List<Participant> participants = participantMapper.selectByActivityId(activityId);
            if (participants.isEmpty()) {
                return java.util.Collections.emptyList();
            }

            // 提取参与者ID列表
            java.util.List<Long> participantIds = participants.stream()
                    .map(Participant::getUserId)
                    .collect(java.util.stream.Collectors.toList());

            return participantIds;

        } catch (Exception e) {
            log.error("查询活动参与者失败: {}", e.getMessage());
            throw new RuntimeException("查询参与者失败: " + e.getMessage());
        }
    }
}