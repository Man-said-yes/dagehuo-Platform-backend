package com.gdmu.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后端统一返回结果
 */
@Data
@Schema(name = "Result", description = "统一返回结果")
public class Result {

    @Schema(description = "状态码：1成功，0失败", example = "1")
    private Integer code; //编码：1成功，0为失败
    
    @Schema(description = "错误信息", example = "success")
    private String msg; //错误信息
    
    @Schema(description = "返回数据")
    private Object data; //数据
    
    //成功
    public static Result success() {
        Result result = new Result();
        result.code = 1;
        result.msg = "success";
        return result;
    }
    
    // 带数据
    public static Result success(Object object) {
        Result result = new Result();
        result.data = object;
        result.code = 1;
        result.msg = "success";
        return result;
    }
    
    public static Result success(String msg, Object object) {
        Result result = new Result();
        result.data = object;
        result.code = 1;
        result.msg = "success";
        return result;
    }
    
    // 失败
    public static Result error(String msg) {
        Result result = new Result();
        result.msg = msg;
        result.code = 0;
        return result;
    }

}
