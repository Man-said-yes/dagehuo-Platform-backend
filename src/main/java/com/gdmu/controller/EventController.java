package com.gdmu.controller;

import com.gdmu.pojo.CreateEventRequest;
import com.gdmu.pojo.Activity;
import com.gdmu.pojo.Result;
import com.gdmu.service.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/event")
@Validated
@CrossOrigin(origins = "*")
@Tag(name = "活动管理", description = "活动发布和管理接口")
public class EventController {

    @Autowired
    private ActivityService activityService;

    @Operation(summary = "发布活动", description = "创建新的搭伙活动")
    @PostMapping
    public Result createEvent(
            jakarta.servlet.http.HttpServletRequest httpRequest,
            @RequestBody CreateEventRequest request) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        try {
            // 验证必填参数
            if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                return Result.error("活动标题不能为空");
            }
            if (request.getLocation() == null || request.getLocation().trim().isEmpty()) {
                return Result.error("活动地点不能为空");
            }
            if (request.getEventTime() == null) {
                return Result.error("活动时间不能为空");
            }
            if (request.getMaxPeople() == null || request.getMaxPeople() <= 0) {
                return Result.error("最大参与人数必须大于0");
            }

            // 创建活动
            Activity activity = activityService.createActivity(
                    userId,
                    request.getType(),
                    request.getTitle(),
                    request.getDescription(),
                    request.getLocation(),
                    request.getEventTime(),
                    request.getMaxPeople()
            );

            return Result.success(activity);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取活动详情", description = "根据活动ID获取活动详细信息")
    @GetMapping("/{eventId}")
    public Result getEventDetail(@PathVariable Long eventId) {
        try {
            Activity activity = activityService.getActivityById(eventId);
            if (activity == null) {
                return Result.error("活动不存在");
            }
            return Result.success(activity);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取活动列表", description = "获取所有活动列表")
    @GetMapping("/list")
    public Result getEventList(@RequestParam(value = "type", required = false) Integer type) {
        try {
            List<Activity> events;
            if (type != null) {
                events = activityService.getActivitiesByType(type);
            } else {
                events = activityService.getAllActivities();
            }
            return Result.success(events);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取我的活动", description = "获取当前用户创建的活动")
    @GetMapping("/my")
    public Result getMyEvents(jakarta.servlet.http.HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            List<Activity> events = activityService.getActivitiesByCreatorId(userId);
            return Result.success(events);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "更新活动", description = "更新活动信息")
    @PutMapping("/{eventId}")
    public Result updateEvent(
            @PathVariable Long eventId,
            jakarta.servlet.http.HttpServletRequest httpRequest,
            @RequestBody CreateEventRequest request) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            // 验证活动是否存在且属于当前用户
            Activity existingActivity = activityService.getActivityById(eventId);
            if (existingActivity == null) {
                return Result.error("活动不存在");
            }
            if (!existingActivity.getCreatorId().equals(userId)) {
                return Result.error("无权限更新此活动");
            }

            // 更新活动
            activityService.updateActivity(
                    eventId,
                    request.getTitle(),
                    request.getDescription(),
                    request.getLocation(),
                    request.getEventTime(),
                    request.getMaxPeople(),
                    request.getType(),
                    existingActivity.getStatus() // 保持原状态
            );

            return Result.success("活动更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "删除活动", description = "删除活动")
    @DeleteMapping("/{eventId}")
    public Result deleteEvent(
            @PathVariable Long eventId,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            // 验证活动是否存在且属于当前用户
            Activity existingActivity = activityService.getActivityById(eventId);
            if (existingActivity == null) {
                return Result.error("活动不存在");
            }
            if (!existingActivity.getCreatorId().equals(userId)) {
                return Result.error("无权限删除此活动");
            }

            // 删除活动
            activityService.deleteActivity(eventId);
            return Result.success("活动删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}