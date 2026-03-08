package com.gdmu.pojo;

import lombok.Data;

import java.util.Date;
@Data
public class User {
    private Long id;
    private String openid;      // 微信唯一标识
    private String studentId;   // 学号
    private String nickname;    // 昵称
    private String avatar;      // 头像URL
    private Integer gender;     // 性别：0未知，1男，2女
    private String phone;       // 手机号
    private Integer creditScore; // 信誉分
    private Integer highCredit; // 高信誉分标识：0否，1是
    private String role; // 角色：user普通用户，admin管理员
    private String password; // 密码（管理员登录用）
    private Date createTime;
    private Date updateTime;
}
