package com.gdmu.service.impl;

import com.gdmu.mapper.MealEventMapper;
import com.gdmu.pojo.MealEvent;
import com.gdmu.service.MealEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class MealEventServiceImpl implements MealEventService {

    @Autowired
    private MealEventMapper mealEventMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MealEvent createEvent(Long creatorId, Integer type, String title, String description, String location, java.util.Date eventTime, Integer maxPeople) {
        log.info("创建新活动，creatorId: {}, type: {}, title: {}, location: {}, eventTime: {}, maxPeople: {}",
                creatorId, type, title, location, eventTime, maxPeople);

        try {
            // 验证 creatorId 不为 null
            if (creatorId == null) {
                throw new RuntimeException("用户ID不能为空");
            }

            MealEvent mealEvent = new MealEvent();
            mealEvent.setTitle(title);
            mealEvent.setDescription(description);
            mealEvent.setLocation(location);
            mealEvent.setEventTime(eventTime);
            mealEvent.setMaxPeople(maxPeople);
            mealEvent.setCurrentPeople(0); // 初始参与人数为0
            mealEvent.setStatus(1); // 初始状态为招募中
            mealEvent.setType(type != null ? type : 0); // 默认类型为其他
            mealEvent.setCreatorId(creatorId);

            int rows = mealEventMapper.insert(mealEvent);
            if (rows <= 0) {
                throw new RuntimeException("创建活动失败");
            }

            log.info("活动创建成功，eventId: {}", mealEvent.getId());
            return mealEvent;

        } catch (Exception e) {
            log.error("创建活动失败: {}", e.getMessage());
            throw new RuntimeException("创建活动失败: " + e.getMessage());
        }
    }

    @Override
    public MealEvent getEventById(Long id) {
        log.info("查询活动信息，eventId: {}", id);
        return mealEventMapper.selectById(id);
    }

    @Override
    public List<MealEvent> getAllEvents() {
        log.info("查询所有活动");
        return mealEventMapper.selectAll();
    }

    @Override
    public List<MealEvent> getEventsByType(Integer type) {
        log.info("查询活动类型: {}", type);
        return mealEventMapper.selectByType(type);
    }

    @Override
    public List<MealEvent> getEventsByCreatorId(Long creatorId) {
        log.info("查询用户创建的活动，creatorId: {}", creatorId);
        return mealEventMapper.selectByCreatorId(creatorId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEvent(Long eventId, String title, String description, String location, java.util.Date eventTime, Integer maxPeople, Integer type, Integer status) {
        log.info("更新活动信息，eventId: {}", eventId);

        try {
            MealEvent mealEvent = mealEventMapper.selectById(eventId);
            if (mealEvent == null) {
                throw new RuntimeException("活动不存在");
            }

            if (title != null) {
                mealEvent.setTitle(title);
            }
            if (description != null) {
                mealEvent.setDescription(description);
            }
            if (location != null) {
                mealEvent.setLocation(location);
            }
            if (eventTime != null) {
                mealEvent.setEventTime(eventTime);
            }
            if (maxPeople != null) {
                mealEvent.setMaxPeople(maxPeople);
            }
            if (type != null) {
                mealEvent.setType(type);
            }
            if (status != null) {
                mealEvent.setStatus(status);
            }

            int rows = mealEventMapper.update(mealEvent);
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
    public void deleteEvent(Long eventId) {
        log.info("删除活动，eventId: {}", eventId);

        try {
            MealEvent mealEvent = mealEventMapper.selectById(eventId);
            if (mealEvent == null) {
                throw new RuntimeException("活动不存在");
            }

            int rows = mealEventMapper.deleteById(eventId);
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