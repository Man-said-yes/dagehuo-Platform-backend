package com.gdmu.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(name = "BindRequest", description = "绑定学号请求")
public class BindRequest {
    @NotBlank(message = "openid不能为空")
    @Schema(description = "微信openid", example = "o1234567890abcdef")
    private String openid;

    @NotBlank(message = "学号不能为空")
    @Pattern(regexp = "^24\\d{9}$", message = "学号格式不正确")
    @Schema(description = "学号，24开头11位数字", example = "24000000000")
    private String studentId;
}
