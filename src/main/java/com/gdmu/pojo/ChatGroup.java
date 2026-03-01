package com.gdmu.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class ChatGroup {
    private Long id;
    private String name;
    private Long activityId;
    private Date createTime;
}
