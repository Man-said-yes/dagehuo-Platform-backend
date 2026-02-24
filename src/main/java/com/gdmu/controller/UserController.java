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

    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户详细信息")
    @GetMapping("/info/{userId}")
    public Result getUserInfo(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }
            return Result.success(user);
        } catch (Exception e) {
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }

    @Operation(summary = "更新用户信息", description = "更新用户的昵称、头像、性别、手机号等信息")
    @PutMapping("/info/{userId}")
    public Result updateUserInfo(
            @PathVariable Long userId,
            @RequestBody UpdateUserRequest request) {
        try {
            userService.updateUser(userId, request.getNickname(), request.getAvatar(), 
                                    request.getGender(), request.getPhone());
            return Result.success("更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "修改昵称", description = "单独修改用户昵称")
    @PutMapping("/nickname/{userId}")
    public Result updateNickname(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request) {
        try {
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
    @PutMapping("/credit/{userId}")
    public Result updateCreditScore(
            @PathVariable Long userId,
            @RequestBody Map<String, Integer> request) {
        try {
            Integer creditScore = request.get("creditScore");
            if (creditScore == null) {
                return Result.error("信誉分不能为空");
            }
            userService.updateCreditScore(userId, creditScore);
            return Result.success("信誉分更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
