package com.gdmu.controller;

import com.gdmu.pojo.Result;
import com.gdmu.pojo.UpdateUserRequest;
import com.gdmu.pojo.User;
import com.gdmu.service.UserService;
import com.gdmu.util.AliyunOssUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @Autowired
    private AliyunOssUtil aliyunOssUtil;

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
    
    @Operation(summary = "修改手机号", description = "修改当前用户的手机号")
    @PutMapping("/phone")
    public Result updatePhone(
            jakarta.servlet.http.HttpServletRequest httpRequest,
            @RequestBody Map<String, String> request) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            String phone = request.get("phone");
            if (phone == null || phone.trim().isEmpty()) {
                return Result.error("手机号不能为空");
            }
            userService.updatePhone(userId, phone);
            return Result.success("手机号更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "上传头像", description = "上传用户头像到阿里云 OSS")
    @PostMapping("/avatar")
    public Result uploadAvatar(
            jakarta.servlet.http.HttpServletRequest httpRequest,
            @RequestParam("file") MultipartFile file) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            
            // 检查文件是否为空
            if (file == null || file.isEmpty()) {
                return Result.error("请选择要上传的头像文件");
            }
            
            // 检查文件类型
            String contentType = file.getContentType();
            if (!contentType.startsWith("image/")) {
                return Result.error("请上传图片文件");
            }
            
            // 检查文件大小（限制 5MB）
            if (file.getSize() > 5 * 1024 * 1024) {
                return Result.error("文件大小不能超过 5MB");
            }
            
            // 上传文件到阿里云 OSS
            String avatarUrl = aliyunOssUtil.uploadFile(file, "avatars");
            
            // 更新用户头像信息
            userService.updateAvatar(userId, avatarUrl);
            
            // 返回头像 URL
            Map<String, String> result = new HashMap<>();
            result.put("avatarUrl", avatarUrl);
            return Result.success(result, "头像上传成功");
        } catch (Exception e) {
            return Result.error("头像上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "修改性别", description = "修改当前用户的性别")
    @PutMapping("/gender")
    public Result updateGender(
            jakarta.servlet.http.HttpServletRequest httpRequest,
            @RequestBody Map<String, Integer> request) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            Integer gender = request.get("gender");
            
            // 验证性别值
            if (gender == null) {
                return Result.error("性别不能为空");
            }
            if (gender < 0 || gender > 2) {
                return Result.error("无效的性别值，应为0-未知，1-男，2-女");
            }
            
            userService.updateGender(userId, gender);
            return Result.success("性别更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "更新当前用户信誉分", description = "更新当前登录用户的信誉分")
    @PutMapping("/credit")
    public Result updateCurrentUserCreditScore(
            jakarta.servlet.http.HttpServletRequest httpRequest,
            @RequestBody Map<String, Integer> request) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
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
