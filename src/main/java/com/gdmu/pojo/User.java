package com.gdmu.pojo;

import lombok.Data;

import java.util.Date;
@Data
public class User {
    private Long id;
    private String openid;      // 微信唯一标识
    private String studentId;   // 学号
    private String nickname;    // 昵称
    private Date createTime;
    private Date updateTime;
}
