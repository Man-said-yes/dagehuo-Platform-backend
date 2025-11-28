package com.gdmu.pojo;// com.gdmu.pojo.LoginRequest.java

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
//openid不能为空,检验加存储器
@Data
public class LoginRequest {
    @NotBlank(message = "code不能为空")
    private String openid;
}

// BindRequest.java


