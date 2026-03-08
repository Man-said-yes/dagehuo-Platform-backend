package com.gdmu.service;

import com.gdmu.pojo.WechatLoginResponse;

public interface AuthService {
    WechatLoginResponse wechatLogin(String code);

    void bindStudent(String openid, String studentId);
    
    void bindStudent(Long userId, String studentId);
    
    WechatLoginResponse adminLogin(String username, String password);
}
