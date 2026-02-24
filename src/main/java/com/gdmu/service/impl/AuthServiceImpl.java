package com.gdmu.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdmu.mapper.UserMapper;
import com.gdmu.pojo.User;
import com.gdmu.pojo.WechatCode2SessionResponse;
import com.gdmu.pojo.WechatLoginResponse;
import com.gdmu.service.AuthService;
import com.gdmu.util.JwtUtil;
import com.gdmu.util.NicknameGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Transactional
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${wechat.app-id}")
    private String appId;

    @Value("${wechat.app-secret}")
    private String appSecret;

    @Value("${wechat.test-mode:false}")
    private boolean testMode;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public WechatLoginResponse wechatLogin(String code) {
        log.info("处理微信登录: {}", code);

        try {
            String openid = getOpenidFromWechat(code);
            if (openid == null) {
                throw new RuntimeException("获取微信openid失败");
            }

            User user = userMapper.selectByOpenid(openid);
            WechatLoginResponse response = new WechatLoginResponse();

            if (user != null) {
                String token = jwtUtil.generateToken(user.getId(), openid);
                response.setToken(token);
                response.setRegistered(user.getStudentId() != null);
                response.setStudentId(user.getStudentId());
                response.setUserId(user.getId());
                log.info("老用户登录成功，userId: {}", user.getId());
            } else {
                User newUser = new User();
                newUser.setOpenid(openid);
                String randomNickname = NicknameGenerator.generate();
                newUser.setNickname(randomNickname);
                newUser.setCreditScore(100); // 设置默认信誉分
                userMapper.insert(newUser);

                String token = jwtUtil.generateToken(newUser.getId(), openid);
                response.setToken(token);
                response.setRegistered(false);
                response.setUserId(newUser.getId());
                log.info("新用户注册成功，userId: {}", newUser.getId());
            }

            return response;

        } catch (Exception e) {
            log.error("微信登录处理失败: {}", e.getMessage());
            throw new RuntimeException("登录处理失败: " + e.getMessage());
        }
    }

    private String getOpenidFromWechat(String code) {
        if (testMode) {
            log.info("测试模式开启，直接使用code作为openid: {}", code);
            return code;
        }

        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                appId, appSecret, code
        );

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            WechatCode2SessionResponse wechatResponse = objectMapper.readValue(
                    response.getBody(), WechatCode2SessionResponse.class
            );

            if (wechatResponse.getErrcode() != null && wechatResponse.getErrcode() != 0) {
                log.error("微信接口返回错误: {}", wechatResponse.getErrmsg());
                return null;
            }

            return wechatResponse.getOpenid();
        } catch (Exception e) {
            log.error("调用微信接口失败: {}", e.getMessage());
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindStudent(String openid, String studentId) {
        log.info("绑定学号，openid: {}, studentId: {}", openid, studentId);

        try {
            if (!studentId.matches("^24\\d{9}$")) {
                throw new RuntimeException("学号格式不正确（24开头11位数字）");
            }

            if (userMapper.existsByStudentId(studentId) > 0) {
                throw new RuntimeException("该学号已被绑定");
            }

            User user = userMapper.selectByOpenid(openid);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }

            user.setStudentId(studentId);
            userMapper.update(user);

            log.info("绑定成功，用户ID: {}", user.getId());

        } catch (Exception e) {
            log.error("绑定学号失败: {}", e.getMessage());
            throw new RuntimeException("绑定失败: " + e.getMessage());
        }
    }
}
