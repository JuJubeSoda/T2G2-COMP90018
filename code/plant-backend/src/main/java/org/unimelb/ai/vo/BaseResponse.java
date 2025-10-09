package org.unimelb.ai.vo;

import com.google.gson.JsonElement;
import lombok.Data;

@Data
public class BaseResponse<T> {
    private Integer code;
    private String msg;
    private T data;

    public BaseResponse() {}

    public BaseResponse(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

}
