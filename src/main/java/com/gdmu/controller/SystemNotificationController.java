package com.gdmu.controller;

import com.gdmu.pojo.Result;
import com.gdmu.service.SystemNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notification")
@Validated
@CrossOrigin(origins = "*")
@Tag(name = "系统通知", description = "系统通知管理接口")
public class SystemNotificationController {

    @Autowired
    private SystemNotificationService systemNotificationService;

    @Operation(summary = "获取用户的系统通知列表", description = "获取当前用户的所有系统通知，按创建时间倒序")
    @GetMapping("/list")
    public Result getNotificationList(jakarta.servlet.http.HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            var notifications = systemNotificationService.getUserNotifications(userId);
            return Result.success(notifications);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "根据类型获取系统通知", description = "根据通知类型获取当前用户的系统通知")
    @GetMapping("/list/type")
    public Result getNotificationsByType(
            jakarta.servlet.http.HttpServletRequest httpRequest,
            @RequestParam(value = "type", required = true) Integer type) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            var notifications = systemNotificationService.getUserNotificationsByType(userId, type);
            return Result.success(notifications);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取未读通知数量", description = "获取当前用户的未读系统通知数量")
    @GetMapping("/unread/count")
    public Result getUnreadCount(jakarta.servlet.http.HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            int count = systemNotificationService.getUnreadNotificationCount(userId);
            return Result.success(count);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "标记通知为已读", description = "将指定的系统通知标记为已读")
    @PutMapping("/{notificationId}/read")
    public Result markAsRead(
            @PathVariable Long notificationId,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        try {
            systemNotificationService.markAsRead(notificationId);
            return Result.success("标记已读成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "批量标记通知为已读", description = "批量将系统通知标记为已读")
    @PutMapping("/read/batch")
    public Result markMultipleAsRead(
            jakarta.servlet.http.HttpServletRequest httpRequest,
            @RequestBody List<Long> notificationIds) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            systemNotificationService.markMultipleAsRead(userId, notificationIds);
            return Result.success("批量标记已读成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}