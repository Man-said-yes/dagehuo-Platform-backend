package com.gdmu.controller;

import com.gdmu.pojo.BindRequest;
import com.gdmu.pojo.LoginRequest;
import com.gdmu.pojo.Result;
import com.gdmu.service.impl.AuthServiceImpl;
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
public class AuthController {

        @Autowired
        private AuthServiceImpl authService;
        //登录
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
        @GetMapping("/test")
        public Result test() {
            Map<String, Object> data = new HashMap<>();
            data.put("message", "服务正常");
            data.put("timestamp", System.currentTimeMillis());
            return Result.success(data);
        }
    }
