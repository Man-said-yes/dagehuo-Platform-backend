package com.gdmu.service;

import java.util.Map;

public interface AuthService {
    /**
     * 微信登录
     * @param code 微信登录code
     * @return 登录结果
     */
    //openid检验加存储器
    Map<String, Object> wechatLogin(String code);

    /**
     * 绑定学号
     * @param openid 微信openid
     * @param studentId 学号
     */
    void bindStudent(String openid, String studentId);
}
