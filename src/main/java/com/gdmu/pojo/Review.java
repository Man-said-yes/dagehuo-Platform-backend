package com.gdmu.pojo;

import lombok.Data;

import java.util.Date;

/**
 * 评价实体类
 */
@Data
public class Review {
    private Long id;                 // 主键ID
    private Long mealEventId;        // 搭伙活动ID
    private Long reviewerId;         // 评价者ID
    private Long reviewedId;         // 被评价者ID
    private Integer rating;          // 评分：1-5星
    private String content;          // 评价内容
    private Date createTime;         // 创建时间
}
