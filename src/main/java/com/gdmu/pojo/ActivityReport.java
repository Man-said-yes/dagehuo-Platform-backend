package com.gdmu.pojo;

import lombok.Data;

import java.util.Date;

/**
 * 活动举报实体类
 */
@Data
public class ActivityReport {
    private Long id;                 // 自增主键（唯一标识一条举报记录）
    private Long activityId;        // 外键，关联 activity.id（哪个活动被举报）
    private Long reporterUserId;     // 外键，关联 users.id（谁举报的，记录用户 ID 保护隐私）
    private String reportReason;     // 举报理由（如 "活动虚假""涉违规内容"，前端可做下拉选择 + 自定义输入）
    private Date reportTime;         // 举报时间（自动填充当前时间）
    private Integer handleStatus;    // 处理状态：0 = 未处理，1 = 已核实（下架活动），2 = 已驳回（举报不成立）
    private Date handleTime;         // 处理时间（处理时自动填充）
    private Integer aiSuggestion;    // AI建议：0 = 默认（无建议），1 = AI建议下架
    private Integer aiSuggested;     // AI是否建议过：0 = 默认（未建议），1 = AI已建议过
}