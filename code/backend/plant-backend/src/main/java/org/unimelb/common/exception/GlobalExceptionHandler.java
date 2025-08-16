package org.unimelb.common.exception;

import org.unimelb.common.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e){
        e.printStackTrace();
        log.error(e.getMessage());
        return Result.fail(299,"系统异常，请联系管理员");
    }
}
