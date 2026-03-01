package com.gdmu.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class ChatMessage {
    private Long id;
    private Long groupId;
    private Long userId;
    private String content;
    private Integer type;
    private Date sendTime;
}
