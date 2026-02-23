package com.gdmu.pojo;// com.gdmu.pojo.LoginRequest.java

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
//openid不能为空,检验加存储器
@Data
@Schema(name = "LoginRequest", description = "登录请求")
public class LoginRequest {
    @NotBlank(message = "openid不能为空")
    @Schema(description = "微信openid", example = "o1234567890abcdef")
    private String openid;
}

// BindRequest.java


