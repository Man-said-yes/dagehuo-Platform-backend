package com.gdmu.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class UserCreditRecord {
    private Long id; // 主键ID
    private Long userId; // 用户ID
    private Integer creditChange; // 信誉分变化值（正数加分，负数扣分）
    private String reason; // 变化原因
    private Date createTime; // 记录时间
}