package com.gdmu.service;

import com.gdmu.pojo.User;

public interface UserService {
    User getUserById(Long id);
    
    User getUserByOpenid(String openid);
    
    void updateUser(Long userId, String nickname, String avatar, Integer gender, String phone);
    
    void updateNickname(Long userId, String nickname);
    
    void updateCreditScore(Long userId, Integer creditScore);
    
    void updatePhone(Long userId, String phone);
    
    void updateAvatar(Long userId, String avatar);
    
    void updateGender(Long userId, Integer gender);
    
    /**
     * 给用户添加信誉分（靠谱）
     */
    void addCreditScore(Long userId);
    
    /**
     * 给用户扣除信誉分（鸽子）
     */
    void deductCreditScore(Long userId);
    
    /**
     * 查询用户的信誉分历史记录
     */
    java.util.List<com.gdmu.pojo.UserCreditRecord> getCreditRecordHistory(Long userId);
    
    /**
     * 获取所有用户列表
     */
    java.util.List<User> getAllUsers();
}
