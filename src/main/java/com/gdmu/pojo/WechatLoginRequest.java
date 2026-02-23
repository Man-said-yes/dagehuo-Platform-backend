package com.gdmu.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(name = "WechatLoginRequest", description = "微信登录请求")
public class WechatLoginRequest {
    @NotBlank(message = "code不能为空")
    @Schema(description = "微信登录code", example = "023abc123def456")
    private String code;
}
