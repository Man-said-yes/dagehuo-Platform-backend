package com.gdmu.controller;

import com.gdmu.pojo.CreateEventRequest;
import com.gdmu.pojo.DistanceQueryRequest;
import com.gdmu.pojo.Activity;
import com.gdmu.pojo.Result;
import com.gdmu.pojo.AISearchRequest;
import com.gdmu.pojo.AIResponse;
import com.gdmu.service.ActivityService;
import com.gdmu.service.AIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/event")
@Validated
@CrossOrigin(origins = "*")
@Tag(name = "活动管理", description = "活动发布和管理接口")
public class EventController {

    @Autowired
    private ActivityService activityService;
    
    @Autowired
    private AIService aiService;

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
                    request.getCampus(),
                    request.getLongitude(),
                    request.getLatitude(),
                    request.getEventTime(),
                    request.getMaxPeople()
            );

            return Result.success(activity);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取活动详情", description = "根据活动ID获取活动详细信息，包含参与者ID列表")
    @GetMapping("/{eventId}")
    public Result getEventDetail(@PathVariable Long eventId) {
        try {
            Activity activity = activityService.getActivityById(eventId);
            if (activity == null) {
                return Result.error("活动不存在");
            }
            
            // 获取活动参与者ID列表
            List<Long> participantIds = activityService.getActivityParticipants(eventId);
            
            // 构建响应对象，包含活动信息和参与者ID列表
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("activity", activity);
            response.put("participants", participantIds);
            
            return Result.success(response);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取活动列表", description = "获取所有活动列表，支持按类型筛选、分页和时间排序")
    @GetMapping("/list")
    public Result getEventList(
            @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "page", defaultValue = "1", required = false) Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "createTime", required = false) String sortBy,
            @RequestParam(value = "order", defaultValue = "desc", required = false) String order) {
        try {
            // 验证分页参数
            if (page < 1) page = 1;
            if (pageSize < 1 || pageSize > 100) pageSize = 10;
            
            List<Activity> events;
            int total;
            
            // 验证排序参数
            if (!sortBy.equals("createTime") && !sortBy.equals("eventTime")) {
                sortBy = "createTime"; // 默认按创建时间排序
            }
            if (!order.equals("asc") && !order.equals("desc")) {
                order = "desc"; // 默认倒序
            }
            
            // 转换排序字段名
            if (sortBy.equals("createTime")) {
                sortBy = "create_time";
            } else if (sortBy.equals("eventTime")) {
                sortBy = "event_time";
            }
            
            if (type != null) {
                events = activityService.getActivitiesByType(type, page, pageSize, sortBy, order);
                total = activityService.getActivityCount(type);
            } else {
                events = activityService.getAllActivities(page, pageSize, sortBy, order);
                total = activityService.getActivityCount(null);
            }
            
            // 构建分页响应
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("list", events);
            response.put("total", total);
            response.put("page", page);
            response.put("pageSize", pageSize);
            response.put("totalPages", (total + pageSize - 1) / pageSize);
            
            return Result.success(response);
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
                    request.getCampus(),
                    request.getLongitude(),
                    request.getLatitude(),
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

    @Operation(summary = "加入活动", description = "用户加入指定活动")
    @PostMapping("/{eventId}/join")
    public Result joinEvent(
            @PathVariable Long eventId,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            activityService.joinActivity(eventId, userId);
            return Result.success("加入活动成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "退出活动", description = "用户退出指定活动")
    @PostMapping("/{eventId}/exit")
    public Result exitEvent(
            @PathVariable Long eventId,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            activityService.exitActivity(eventId, userId);
            return Result.success("退出活动成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取我参与的活动", description = "获取当前用户参与的所有活动")
    @GetMapping("/participated")
    public Result getParticipatedEvents(jakarta.servlet.http.HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            List<Activity> events = activityService.getActivitiesByParticipantId(userId);
            return Result.success(events);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "更新活动状态", description = "更新活动状态（1-招募中，2-进行中，3-已结束，4-已取消）")
    @PutMapping("/{eventId}/status")
    public Result updateEventStatus(
            @PathVariable Long eventId,
            jakarta.servlet.http.HttpServletRequest httpRequest,
            @RequestBody java.util.Map<String, Integer> request) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            // 验证活动是否存在且属于当前用户
            Activity existingActivity = activityService.getActivityById(eventId);
            if (existingActivity == null) {
                return Result.error("活动不存在");
            }
            if (!existingActivity.getCreatorId().equals(userId)) {
                return Result.error("无权限更新此活动状态");
            }

            // 获取状态值
            Integer status = request.get("status");
            if (status == null) {
                return Result.error("状态值不能为空");
            }

            // 更新活动状态
            activityService.updateActivityStatus(eventId, status);
            return Result.success("活动状态更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "根据距离查询活动（由近及远）", description = "根据用户当前位置的经度和纬度，由近及远查询活动列表")
    @PostMapping("/list/nearby")
    public Result getNearbyEvents(@RequestBody DistanceQueryRequest request) {
        try {
            // 验证参数
            if (request.getLongitude() == null || request.getLatitude() == null) {
                return Result.error("经纬度不能为空");
            }
            if (request.getPage() < 1) request.setPage(1);
            if (request.getPageSize() < 1 || request.getPageSize() > 100) request.setPageSize(10);
            
            List<Activity> events = activityService.getActivitiesByDistanceAsc(
                    request.getLongitude(), 
                    request.getLatitude(), 
                    request.getType(), 
                    request.getPage(), 
                    request.getPageSize()
            );
            int total = activityService.getActivityCountByDistance(request.getType());
            
            // 构建分页响应
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("list", events);
            response.put("total", total);
            response.put("page", request.getPage());
            response.put("pageSize", request.getPageSize());
            response.put("totalPages", (total + request.getPageSize() - 1) / request.getPageSize());
            
            return Result.success(response);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "根据距离查询活动（由远及近）", description = "根据用户当前位置的经度和纬度，由远及近查询活动列表")
    @PostMapping("/list/distant")
    public Result getDistantEvents(@RequestBody DistanceQueryRequest request) {
        try {
            // 验证参数
            if (request.getLongitude() == null || request.getLatitude() == null) {
                return Result.error("经纬度不能为空");
            }
            if (request.getPage() < 1) request.setPage(1);
            if (request.getPageSize() < 1 || request.getPageSize() > 100) request.setPageSize(10);
            
            List<Activity> events = activityService.getActivitiesByDistanceDesc(
                    request.getLongitude(), 
                    request.getLatitude(), 
                    request.getType(), 
                    request.getPage(), 
                    request.getPageSize()
            );
            int total = activityService.getActivityCountByDistance(request.getType());
            
            // 构建分页响应
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("list", events);
            response.put("total", total);
            response.put("page", request.getPage());
            response.put("pageSize", request.getPageSize());
            response.put("totalPages", (total + request.getPageSize() - 1) / request.getPageSize());
            
            return Result.success(response);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取活动参与者详情", description = "获取指定活动的所有参与者详细信息")
    @GetMapping("/{eventId}/participants")
    public Result getEventParticipantsInfo(@PathVariable Long eventId) {
        try {
            // 检查活动是否存在
            Activity activity = activityService.getActivityById(eventId);
            if (activity == null) {
                return Result.error("活动不存在");
            }
            
            // 获取活动参与者详细信息
            List<com.gdmu.pojo.User> participants = activityService.getActivityParticipantsInfo(eventId);
            
            return Result.success(participants);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "举报活动", description = "用户举报指定活动")
    @PostMapping("/{eventId}/report")
    public Result reportEvent(
            @PathVariable Long eventId,
            jakarta.servlet.http.HttpServletRequest httpRequest,
            @RequestBody java.util.Map<String, String> request) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            String reportReason = request.get("reportReason");
            
            if (reportReason == null || reportReason.trim().isEmpty()) {
                return Result.error("举报理由不能为空");
            }
            
            activityService.reportActivity(eventId, userId, reportReason);
            return Result.success("举报成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取被举报活动列表", description = "获取所有被举报活动的列表，支持分页")
    @GetMapping("/report/list")
    public Result getReportedEvents(
            @RequestParam(value = "page", defaultValue = "1", required = false) Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
        try {
            // 验证分页参数
            if (page < 1) page = 1;
            if (pageSize < 1 || pageSize > 100) pageSize = 10;
            
            List<com.gdmu.pojo.ActivityReport> reports = activityService.getReportedActivities(page, pageSize);
            int total = activityService.getReportedActivityCount();
            
            // 构建分页响应
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("list", reports);
            response.put("total", total);
            response.put("page", page);
            response.put("pageSize", pageSize);
            response.put("totalPages", (total + pageSize - 1) / pageSize);
            
            return Result.success(response);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "根据处理状态获取被举报活动列表", description = "根据处理状态获取被举报活动的列表，支持分页")
    @GetMapping("/report/list/status")
    public Result getReportedEventsByStatus(
            @RequestParam(value = "status", required = true) Integer status,
            @RequestParam(value = "page", defaultValue = "1", required = false) Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
        try {
            // 验证分页参数
            if (page < 1) page = 1;
            if (pageSize < 1 || pageSize > 100) pageSize = 10;
            
            List<com.gdmu.pojo.ActivityReport> reports = activityService.getReportedActivitiesByStatus(status, page, pageSize);
            int total = activityService.getReportedActivityCountByStatus(status);
            
            // 构建分页响应
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("list", reports);
            response.put("total", total);
            response.put("page", page);
            response.put("pageSize", pageSize);
            response.put("totalPages", (total + pageSize - 1) / pageSize);
            
            return Result.success(response);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "取消活动", description = "取消指定活动（设置状态为4-已取消）")
    @PostMapping("/{eventId}/cancel")
    public Result cancelEvent(
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
                return Result.error("无权限取消此活动");
            }

            // 取消活动（设置状态为4）
            activityService.updateActivityStatus(eventId, 4);
            return Result.success("活动取消成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "结束活动", description = "结束指定活动（设置状态为3-已结束）")
    @PostMapping("/{eventId}/end")
    public Result endEvent(
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
                return Result.error("无权限结束此活动");
            }

            // 结束活动（设置状态为3）
            activityService.updateActivityStatus(eventId, 3);
            return Result.success("活动结束成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "核实举报", description = "将举报标记为已核实并结束活动，通知所有参与用户")
    @PostMapping("/report/{reportId}/verify")
    public Result verifyReport(
            @PathVariable Long reportId,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            // 验证用户权限（这里可以添加管理员权限验证）
            
            // 核实举报并结束活动
            activityService.verifyReport(reportId);
            return Result.success("举报核实成功，活动已结束");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "驳回举报", description = "将举报标记为已驳回")
    @PostMapping("/report/{reportId}/reject")
    public Result rejectReport(
            @PathVariable Long reportId,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            // 验证用户权限（这里可以添加管理员权限验证）
            
            // 驳回举报
            activityService.rejectReport(reportId);
            return Result.success("举报驳回成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @Operation(summary = "AI智能查询活动", description = "根据用户查询语句和位置信息，使用AI推荐最匹配的活动")
    @PostMapping("/ai/search")
    public Result aiSearchEvent(
            jakarta.servlet.http.HttpServletRequest httpRequest,
            @RequestBody AISearchRequest request) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            // 验证参数
            if (request.getQuery() == null || request.getQuery().trim().isEmpty()) {
                return Result.error("查询语句不能为空");
            }
            if (request.getLongitude() == null || request.getLatitude() == null) {
                return Result.error("经纬度不能为空");
            }
            
            // 获取所有活动列表
            List<Activity> activities = activityService.getAllActivities(1, 100, "create_time", "desc");
            
            // 调用AI服务获取推荐活动
            AIResponse aiResponse = aiService.getRecommendedActivities(
                    request.getQuery(),
                    request.getLongitude(),
                    request.getLatitude(),
                    activities
            );
            
            return Result.success(aiResponse);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @Operation(summary = "AI兴趣推荐活动", description = "根据用户历史参与活动分析兴趣偏好，推荐最匹配的活动")
    @PostMapping("/ai/interest")
    public Result aiInterestRecommendEvent(@RequestBody AISearchRequest request, HttpServletRequest httpRequest) {
        try {
            // 获取当前用户ID
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            if (request.getLongitude() == null || request.getLatitude() == null) {
                return Result.error("经纬度不能为空");
            }
            
            // 获取用户参加过的活动
            List<Activity> userActivities = activityService.getActivitiesByParticipantId(userId);
            
            // 获取所有招募中的活动
            List<Activity> recruitingActivities = activityService.getAllActivities(1, 100, "create_time", "desc").stream()
                    .filter(activity -> activity.getStatus() == 1)
                    .collect(Collectors.toList());
            
            // 调用AI服务获取兴趣推荐活动
            AIResponse aiResponse = aiService.getInterestRecommendedActivities(
                    userId,
                    request.getLongitude(),
                    request.getLatitude(),
                    recruitingActivities,
                    userActivities
            );
            
            return Result.success(aiResponse);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @Operation(summary = "AI建议举报处理", description = "AI对未建议的举报进行分析并给出建议")
    @PostMapping("/ai/suggest")
    public Result aiSuggestReports(@RequestBody java.util.Map<String, Integer> request) {
        try {
            Integer enable = request.get("enable");
            if (enable == null) {
                return Result.error("开关参数不能为空");
            }
            
            if (enable == 1) {
                activityService.aiSuggestReports();
                return Result.success("AI建议处理完成");
            } else {
                return Result.success("AI建议处理已关闭");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @Operation(summary = "AI自动审核举报", description = "AI自动审核举报并下架违规活动")
    @PostMapping("/ai/auto-review")
    public Result aiAutoReview(@RequestBody java.util.Map<String, Integer> request) {
        try {
            Integer enable = request.get("enable");
            if (enable == null) {
                return Result.error("开关参数不能为空");
            }
            
            if (enable == 1) {
                activityService.aiAutoReview();
                return Result.success("AI自动审核完成");
            } else {
                return Result.success("AI自动审核已关闭");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}