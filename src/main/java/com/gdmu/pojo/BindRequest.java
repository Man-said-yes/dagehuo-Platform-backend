package com.gdmu.pojo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class BindRequest {
    @NotBlank(message = "openid不能为空")
    private String openid;

    @NotBlank(message = "学号不能为空")
    @Pattern(regexp = "^24\\d{9}$", message = "学号格式不正确")
    private String studentId;
}
