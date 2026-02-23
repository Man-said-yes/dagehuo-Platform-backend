package com.gdmu.pojo;

import lombok.Data;

@Data
public class WechatCode2SessionResponse {
    private String openid;
    private String session_key;
    private String unionid;
    private Integer errcode;
    private String errmsg;
}
