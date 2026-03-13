package com.gdmu.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class ChatGroup {
    private Long id;
    private String name;
    private Integer type;
    private Long activityId;
    private Long ownerId;
    private Date createTime;
    private Integer status;
}
