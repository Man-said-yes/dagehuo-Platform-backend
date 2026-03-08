package com.gdmu.controller;

import com.gdmu.pojo.BindRequest;
import com.gdmu.pojo.Result;
import com.gdmu.pojo.WechatLoginRequest;
import com.gdmu.pojo.WechatLoginResponse;
import com.gdmu.service.impl.AuthServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Validated
@CrossOrigin(origins = "*")
@Tag(name = "认证管理", description = "微信登录、学号绑定等认证相关接口")
public class AuthController {

    @Autowired
    private AuthServiceImpl authService;

    @Operation(summary = "微信登录", description = "通过微信code登录，返回JWT token")
    @PostMapping("/login")
    public Result login(@Valid @RequestBody WechatLoginRequest request) {
        try {
            WechatLoginResponse result = authService.wechatLogin(request.getCode());
            // 只返回token字段
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("TOKEN", result.getToken());
            return Result.success(data);
        } catch (Exception e) {
            return Result.error("登录失败: " + e.getMessage());
        }
    }

    @Operation(summary = "绑定学号", description = "将当前用户与学号绑定")
    @PostMapping("/bind")
    public Result bind(
            jakarta.servlet.http.HttpServletRequest httpRequest,
            @Valid @RequestBody BindRequest request) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            authService.bindStudent(userId, request.getStudentId());

            Map<String, Object> data = new HashMap<>();
            data.put("studentId", request.getStudentId());
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "服务连通性测试", description = "测试服务是否正常运行")
    @GetMapping("/test")
    public Result test() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "服务正常");
        data.put("timestamp", System.currentTimeMillis());
        return Result.success(data);
    }

    @Operation(summary = "管理员登录", description = "管理员专用登录接口，使用用户名密码登录")
    @PostMapping("/admin/login")
    public Result adminLogin(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            
            if (username == null || password == null) {
                return Result.error("用户名和密码不能为空");
            }
            
            WechatLoginResponse result = authService.adminLogin(username, password);
            Map<String, Object> data = new HashMap<>();
            data.put("TOKEN", result.getToken());
            return Result.success(data);
        } catch (Exception e) {
            return Result.error("登录失败: " + e.getMessage());
        }
    }
}
