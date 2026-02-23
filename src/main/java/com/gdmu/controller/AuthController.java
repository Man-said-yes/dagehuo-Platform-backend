package com.gdmu.controller;

import com.gdmu.pojo.BindRequest;
import com.gdmu.pojo.LoginRequest;
import com.gdmu.pojo.Result;
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
    //验证
    @Validated
    //允许跨域请求
    @CrossOrigin(origins = "*")
    //Swagger标签
    @Tag(name = "认证管理", description = "微信登录、学号绑定等认证相关接口")
public class AuthController {

        @Autowired
        private AuthServiceImpl authService;
        //登录
        @Operation(summary = "微信登录", description = "通过微信openid登录，返回用户注册状态")
        @PostMapping("/login")
        public Result login(@Valid @RequestBody LoginRequest request) {
            try {
                Map<String, Object> result = authService.wechatLogin(request.getOpenid());
                return Result.success(result);
            } catch (Exception e) {
                return Result.error("登录失败: " + e.getMessage());
            }
        }
        //绑定学号
        @Operation(summary = "绑定学号", description = "将微信openid与学号绑定")
        @PostMapping("/bind")
        public Result bind(@Valid @RequestBody BindRequest request) {
            try {
                authService.bindStudent(request.getOpenid(), request.getStudentId());

                Map<String, Object> data = new HashMap<>();
                // 返回学号
                data.put("studentId", request.getStudentId());
                return Result.success(data);
            } catch (Exception e) {
                return Result.error(e.getMessage());
            }
        }
        //前端可调用的证明已联通测试
        @Operation(summary = "服务连通性测试", description = "测试服务是否正常运行")
        @GetMapping("/test")
        public Result test() {
            Map<String, Object> data = new HashMap<>();
            data.put("message", "服务正常");
            data.put("timestamp", System.currentTimeMillis());
            return Result.success(data);
        }
    }
