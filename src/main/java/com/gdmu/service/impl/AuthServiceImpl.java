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
                String token = jwtUtil.generateToken(user.getId(), openid, user.getRole());
                response.setToken(token);
                response.setRegistered(user.getStudentId() != null);
                response.setStudentId(user.getStudentId());
                response.setUserId(user.getId());
                log.info("老用户登录成功，userId: {}, role: {}", user.getId(), user.getRole());
            } else {
                User newUser = new User();
                newUser.setOpenid(openid);
                String randomNickname = NicknameGenerator.generate();
                newUser.setNickname(randomNickname);
                newUser.setAvatar("https://jinejie-java-ai.oss-cn-beijing.aliyuncs.com/001.jpg"); // 设置默认头像
                newUser.setCreditScore(100); // 设置默认信誉分
                newUser.setHighCredit(0); // 设置默认高信誉分标识为0
                newUser.setRole("user"); // 设置默认角色为普通用户
                newUser.setPassword(null); // 普通用户密码为null
                userMapper.insert(newUser);

                String token = jwtUtil.generateToken(newUser.getId(), openid, newUser.getRole());
                response.setToken(token);
                response.setRegistered(false);
                response.setUserId(newUser.getId());
                log.info("新用户注册成功，userId: {}, role: {}", newUser.getId(), newUser.getRole());
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
    public void bindStudent(Long userId, String studentId) {
        log.info("绑定学号，userId: {}, studentId: {}", userId, studentId);

        try {
            if (!studentId.matches("^24\\d{9}$")) {
                throw new RuntimeException("学号格式不正确（24开头11位数字）");
            }

            if (userMapper.existsByStudentId(studentId) > 0) {
                throw new RuntimeException("该学号已被绑定");
            }

            User user = userMapper.selectById(userId);
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
    
    // 兼容旧的方法
    @Transactional(rollbackFor = Exception.class)
    public void bindStudent(String openid, String studentId) {
        User user = userMapper.selectByOpenid(openid);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        bindStudent(user.getId(), studentId);
    }

    @Override
    public WechatLoginResponse adminLogin(String username, String password) {
        log.info("处理管理员登录: {}", username);

        try {
            // 查找管理员用户（如果不存在则创建）
            User adminUser = userMapper.selectByOpenid("admin_openid");
            WechatLoginResponse response = new WechatLoginResponse();

            if (adminUser != null) {
                // 检查是否为管理员角色
                if (!"admin".equals(adminUser.getRole())) {
                    throw new RuntimeException("该用户不是管理员");
                }
                // 验证密码
                if (!"admin123".equals(password)) {
                    throw new RuntimeException("用户名或密码错误");
                }
                String token = jwtUtil.generateToken(adminUser.getId(), adminUser.getOpenid(), adminUser.getRole());
                response.setToken(token);
                response.setRegistered(adminUser.getStudentId() != null);
                response.setStudentId(adminUser.getStudentId());
                response.setUserId(adminUser.getId());
                log.info("管理员登录成功，userId: {}", adminUser.getId());
            } else {
                // 创建管理员用户
                User newAdmin = new User();
                newAdmin.setOpenid("admin_openid");
                newAdmin.setNickname("管理员");
                newAdmin.setAvatar("https://jinejie-java-ai.oss-cn-beijing.aliyuncs.com/001.jpg");
                newAdmin.setCreditScore(100);
                newAdmin.setHighCredit(0);
                newAdmin.setRole("admin"); // 设置为管理员角色
                newAdmin.setPassword("admin123"); // 设置默认密码
                userMapper.insert(newAdmin);

                String token = jwtUtil.generateToken(newAdmin.getId(), newAdmin.getOpenid(), newAdmin.getRole());
                response.setToken(token);
                response.setRegistered(false);
                response.setUserId(newAdmin.getId());
                log.info("管理员用户创建成功，userId: {}", newAdmin.getId());
            }

            return response;

        } catch (Exception e) {
            log.error("管理员登录处理失败: {}", e.getMessage());
            throw new RuntimeException("登录处理失败: " + e.getMessage());
        }
    }
    
    @Override
    public WechatLoginResponse register(String username, String password, String nickname) {
        log.info("处理用户注册: {}", username);
        
        try {
            // 检查用户名是否已存在
            User existingUser = userMapper.selectByOpenid(username);
            if (existingUser != null) {
                throw new RuntimeException("用户名已存在");
            }
            
            // 创建新用户
            User newUser = new User();
            newUser.setOpenid(username);
            newUser.setPassword(password);
            newUser.setNickname(nickname != null && !nickname.trim().isEmpty() ? nickname : NicknameGenerator.generate());
            newUser.setAvatar("https://jinejie-java-ai.oss-cn-beijing.aliyuncs.com/001.jpg");
            newUser.setCreditScore(100);
            newUser.setHighCredit(0);
            newUser.setRole("user");
            userMapper.insert(newUser);
            
            // 生成token
            String token = jwtUtil.generateToken(newUser.getId(), username, newUser.getRole());
            
            WechatLoginResponse response = new WechatLoginResponse();
            response.setToken(token);
            response.setUserId(newUser.getId());
            response.setRegistered(false);
            
            log.info("用户注册成功，userId: {}, username: {}", newUser.getId(), username);
            return response;
            
        } catch (Exception e) {
            log.error("用户注册失败: {}", e.getMessage());
            throw new RuntimeException("注册失败: " + e.getMessage());
        }
    }
    
    @Override
    public WechatLoginResponse login(String username, String password) {
        log.info("处理用户登录: {}", username);
        
        try {
            // 查找用户
            User user = userMapper.selectByOpenid(username);
            if (user == null) {
                throw new RuntimeException("用户名或密码错误");
            }
            
            // 验证密码
            if (!password.equals(user.getPassword())) {
                throw new RuntimeException("用户名或密码错误");
            }
            
            // 生成token
            String token = jwtUtil.generateToken(user.getId(), username, user.getRole());
            
            WechatLoginResponse response = new WechatLoginResponse();
            response.setToken(token);
            response.setUserId(user.getId());
            response.setRegistered(user.getStudentId() != null);
            response.setStudentId(user.getStudentId());
            
            log.info("用户登录成功，userId: {}, username: {}", user.getId(), username);
            return response;
            
        } catch (Exception e) {
            log.error("用户登录失败: {}", e.getMessage());
            throw new RuntimeException("登录失败: " + e.getMessage());
        }
    }
}
