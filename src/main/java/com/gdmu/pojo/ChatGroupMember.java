package com.gdmu.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class ChatGroupMember {
    private Long id;
    private Long groupId;
    private Long userId;
    private Date joinTime;
    private Long lastReadMessageId;
}
