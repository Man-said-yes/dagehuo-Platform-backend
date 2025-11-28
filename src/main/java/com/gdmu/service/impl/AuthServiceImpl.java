package com.gdmu.service.impl;

import com.gdmu.mapper.UserMapper;
import com.gdmu.pojo.User;
import com.gdmu.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Transactional
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public Map<String, Object> wechatLogin(String openid) {
        log.info("处理微信登录: {}", openid);

        try {

            // 2查询用户是否存在
            User user = userMapper.selectByOpenid(openid);
            Map<String, Object> result = new HashMap<>();
            if (user != null) {
                // 老用户
                log.info("老用户，学号: {}", user.getStudentId());
                result.put("registered", true);
                result.put("studentId", user.getStudentId());
            } else {
                // 新用户
                log.info("新用户，需要绑定学号");
                result.put("registered", false);
            }

            return result;

        } catch (Exception e) {
            log.error("微信登录处理失败: {}", e.getMessage());
            throw new RuntimeException("登录处理失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindStudent(String openid, String studentId) {
        log.info("绑定学号，openid: {}, studentId: {}", openid, studentId);

        try {
            // 1. 验证学号格式
            if (!studentId.matches("^24\\d{9}$")) {
                throw new RuntimeException("学号格式不正确（24开头11位数字）");
            }

            // 2. 检查学号是否已存在
            if (userMapper.existsByStudentId(studentId) > 0) {
                throw new RuntimeException("该学号已被绑定");
            }

            // 3. 创建用户
            User user = new User();
            user.setOpenid(openid);
            user.setStudentId(studentId);

            // 4. 保存到数据库
            int rows = userMapper.insert(user);
            if (rows <= 0) {
                throw new RuntimeException("保存用户失败");
            }

            log.info("绑定成功，用户ID: {}", user.getId());

        } catch (Exception e) {
            log.error("绑定学号失败: {}", e.getMessage());
            throw new RuntimeException("绑定失败: " + e.getMessage());
        }
    }
}