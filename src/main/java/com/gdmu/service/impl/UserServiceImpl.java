package com.gdmu.service.impl;

import com.gdmu.mapper.UserMapper;
import com.gdmu.mapper.UserCreditRecordMapper;
import com.gdmu.pojo.User;
import com.gdmu.pojo.UserCreditRecord;
import com.gdmu.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private UserCreditRecordMapper userCreditRecordMapper;

    @Override
    public User getUserById(Long id) {
        log.info("查询用户信息，userId: {}", id);
        return userMapper.selectById(id);
    }

    @Override
    public User getUserByOpenid(String openid) {
        log.info("查询用户信息，openid: {}", openid);
        return userMapper.selectByOpenid(openid);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long userId, String nickname, String avatar, Integer gender, String phone) {
        log.info("更新用户信息，userId: {}, nickname: {}, avatar: {}, gender: {}, phone: {}", 
                 userId, nickname, avatar, gender, phone);
        
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            
            if (nickname != null) {
                user.setNickname(nickname);
            }
            if (avatar != null) {
                user.setAvatar(avatar);
            }
            if (gender != null) {
                user.setGender(gender);
            }
            if (phone != null) {
                user.setPhone(phone);
            }
            
            int rows = userMapper.update(user);
            if (rows <= 0) {
                throw new RuntimeException("更新用户信息失败");
            }
            
            log.info("用户信息更新成功，userId: {}", userId);
            
        } catch (Exception e) {
            log.error("更新用户信息失败: {}", e.getMessage());
            throw new RuntimeException("更新用户信息失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNickname(Long userId, String nickname) {
        log.info("更新用户昵称，userId: {}, nickname: {}", userId, nickname);
        
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            
            user.setNickname(nickname);
            
            int rows = userMapper.update(user);
            if (rows <= 0) {
                throw new RuntimeException("更新昵称失败");
            }
            
            log.info("用户昵称更新成功，userId: {}", userId);
            
        } catch (Exception e) {
            log.error("更新用户昵称失败: {}", e.getMessage());
            throw new RuntimeException("更新昵称失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCreditScore(Long userId, Integer creditScore) {
        log.info("更新用户信誉分，userId: {}, creditScore: {}", userId, creditScore);
        
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            
            int rows = userMapper.updateCreditScore(userId, creditScore);
            if (rows <= 0) {
                throw new RuntimeException("更新信誉分失败");
            }
            
            log.info("用户信誉分更新成功，userId: {}, creditScore: {}", userId, creditScore);
            
        } catch (Exception e) {
            log.error("更新用户信誉分失败: {}", e.getMessage());
            throw new RuntimeException("更新信誉分失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePhone(Long userId, String phone) {
        log.info("更新用户手机号，userId: {}, phone: {}", userId, phone);
        
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            
            user.setPhone(phone);
            
            int rows = userMapper.update(user);
            if (rows <= 0) {
                throw new RuntimeException("更新手机号失败");
            }
            
            log.info("用户手机号更新成功，userId: {}", userId);
            
        } catch (Exception e) {
            log.error("更新用户手机号失败: {}", e.getMessage());
            throw new RuntimeException("更新手机号失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAvatar(Long userId, String avatar) {
        log.info("更新用户头像，userId: {}, avatar: {}", userId, avatar);
        
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            
            user.setAvatar(avatar);
            
            int rows = userMapper.update(user);
            if (rows <= 0) {
                throw new RuntimeException("更新头像失败");
            }
            
            log.info("用户头像更新成功，userId: {}", userId);
            
        } catch (Exception e) {
            log.error("更新用户头像失败: {}", e.getMessage());
            throw new RuntimeException("更新头像失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGender(Long userId, Integer gender) {
        log.info("更新用户性别，userId: {}, gender: {}", userId, gender);
        
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            
            // 验证性别值是否合法（0-未知，1-男，2-女）
            if (gender != null && (gender < 0 || gender > 2)) {
                throw new RuntimeException("无效的性别值，应为0-未知，1-男，2-女");
            }
            
            user.setGender(gender);
            
            int rows = userMapper.update(user);
            if (rows <= 0) {
                throw new RuntimeException("更新性别失败");
            }
            
            log.info("用户性别更新成功，userId: {}", userId);
            
        } catch (Exception e) {
            log.error("更新用户性别失败: {}", e.getMessage());
            throw new RuntimeException("更新性别失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCreditScore(Long userId) {
        log.info("给用户添加信誉分（靠谱），userId: {}", userId);
        
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            
            // 计算新的信誉分
            int newCreditScore = user.getCreditScore() + 5;
            
            // 更新用户信誉分
            userMapper.updateCreditScore(userId, newCreditScore);
            
            // 记录信誉分变化
            UserCreditRecord record = new UserCreditRecord();
            record.setUserId(userId);
            record.setCreditChange(5);
            record.setReason("靠谱 +5");
            userCreditRecordMapper.insert(record);
            
            log.info("用户信誉分添加成功，userId: {}, 新信誉分: {}", userId, newCreditScore);
            
        } catch (Exception e) {
            log.error("给用户添加信誉分失败: {}", e.getMessage());
            throw new RuntimeException("添加信誉分失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductCreditScore(Long userId) {
        log.info("给用户扣除信誉分（鸽子），userId: {}", userId);
        
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            
            // 计算新的信誉分
            int newCreditScore = user.getCreditScore() - 5;
            // 确保信誉分不会小于0
            if (newCreditScore < 0) {
                newCreditScore = 0;
            }
            
            // 更新用户信誉分
            userMapper.updateCreditScore(userId, newCreditScore);
            
            // 记录信誉分变化
            UserCreditRecord record = new UserCreditRecord();
            record.setUserId(userId);
            record.setCreditChange(-5);
            record.setReason("鸽子 -5");
            userCreditRecordMapper.insert(record);
            
            log.info("用户信誉分扣除成功，userId: {}, 新信誉分: {}", userId, newCreditScore);
            
        } catch (Exception e) {
            log.error("给用户扣除信誉分失败: {}", e.getMessage());
            throw new RuntimeException("扣除信誉分失败: " + e.getMessage());
        }
    }

    @Override
    public java.util.List<UserCreditRecord> getCreditRecordHistory(Long userId) {
        log.info("查询用户的信誉分历史记录，userId: {}", userId);
        
        try {
            // 检查用户是否存在
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            
            // 查询用户的信誉分历史记录
            return userCreditRecordMapper.selectByUserId(userId);
            
        } catch (Exception e) {
            log.error("查询用户信誉分历史记录失败: {}", e.getMessage());
            throw new RuntimeException("查询信誉分历史记录失败: " + e.getMessage());
        }
    }
}
