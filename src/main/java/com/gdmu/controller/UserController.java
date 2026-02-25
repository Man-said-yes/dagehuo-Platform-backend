package com.gdmu.controller;

import com.gdmu.pojo.Result;
import com.gdmu.pojo.UpdateUserRequest;
import com.gdmu.pojo.User;
import com.gdmu.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@Validated
@CrossOrigin(origins = "*")
@Tag(name = "用户管理", description = "用户信息查询和更新接口")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/info")
    public Result getCurrentUserInfo(jakarta.servlet.http.HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            User user = userService.getUserById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }
            return Result.success(user);
        } catch (Exception e) {
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户详细信息")
    @GetMapping("/info/{targetUserId}")
    public Result getUserInfo(
            jakarta.servlet.http.HttpServletRequest httpRequest,
            @PathVariable Long targetUserId) {
        try {
            // 这里可以根据业务需求，决定是否允许查看其他用户的信息
            // 暂时允许查看
            User user = userService.getUserById(targetUserId);
            if (user == null) {
                return Result.error("用户不存在");
            }
            return Result.success(user);
        } catch (Exception e) {
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }

    @Operation(summary = "更新用户信息", description = "更新当前用户的昵称、头像、性别、手机号等信息")
    @PutMapping("/info")
    public Result updateUserInfo(
            jakarta.servlet.http.HttpServletRequest httpRequest,
            @RequestBody UpdateUserRequest request) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            userService.updateUser(userId, request.getNickname(), request.getAvatar(), 
                                    request.getGender(), request.getPhone());
            return Result.success("更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "修改昵称", description = "修改当前用户的昵称")
    @PutMapping("/nickname")
    public Result updateNickname(
            jakarta.servlet.http.HttpServletRequest httpRequest,
            @RequestBody Map<String, String> request) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            String nickname = request.get("nickname");
            if (nickname == null || nickname.trim().isEmpty()) {
                return Result.error("昵称不能为空");
            }
            userService.updateNickname(userId, nickname);
            return Result.success("昵称更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @Operation(summary = "更新信誉分", description = "更新用户的信誉分")
    @PutMapping("/credit/{targetUserId}")
    public Result updateCreditScore(
            jakarta.servlet.http.HttpServletRequest httpRequest,
            @PathVariable Long targetUserId,
            @RequestBody Map<String, Integer> request) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            // 这里可以根据业务需求，决定是否只有管理员可以更新信誉分
            // 暂时允许所有用户更新（实际项目中应该限制权限）
            Integer creditScore = request.get("creditScore");
            if (creditScore == null) {
                return Result.error("信誉分不能为空");
            }
            userService.updateCreditScore(targetUserId, creditScore);
            return Result.success("信誉分更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
