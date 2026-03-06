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
}
