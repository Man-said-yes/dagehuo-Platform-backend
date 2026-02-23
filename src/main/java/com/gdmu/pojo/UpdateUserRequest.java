package com.gdmu.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "UpdateUserRequest", description = "更新用户信息请求")
public class UpdateUserRequest {
    @Schema(description = "昵称", example = "张三")
    private String nickname;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "性别：0未知，1男，2女", example = "1")
    private Integer gender;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;
}
