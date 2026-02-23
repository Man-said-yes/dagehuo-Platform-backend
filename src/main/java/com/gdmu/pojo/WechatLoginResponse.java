package com.gdmu.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "WechatLoginResponse", description = "微信登录响应")
public class WechatLoginResponse {
    @Schema(description = "JWT token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "用户是否已绑定学号", example = "true")
    private Boolean registered;

    @Schema(description = "学号（如果已绑定）", example = "24000000000")
    private String studentId;

    @Schema(description = "用户ID", example = "1")
    private Long userId;
}
