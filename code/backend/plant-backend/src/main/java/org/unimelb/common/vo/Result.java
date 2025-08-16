package org.unimelb.common.vo;

import org.unimelb.common.constant.ResultConstant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    public static Result success(){
        return new Result(ResultConstant.SUCCESS.getCode(),ResultConstant.SUCCESS.getMessage(),null);
    }

    public static <T> Result<T> success(T data){
        return new Result(ResultConstant.SUCCESS.getCode(), ResultConstant.SUCCESS.getMessage(),data);
    }

    public static <T> Result<T> success(T data,String message){
        return new Result(ResultConstant.SUCCESS.getCode(),message,data);
    }

    public static <T> Result<T> success(String message){
        return new Result(ResultConstant.SUCCESS.getCode(),message,null);
    }

    public static <T> Result<T> fail(){
        return new Result(ResultConstant.FAIL.getCode(),ResultConstant.FAIL.getMessage(),null);
    }

    public static <T> Result<T> fail(Integer code){
        return new Result(code,"fail",null);
    }

    public static <T> Result<T> fail(Integer code,String message){
        return new Result(code,message,null);
    }

}