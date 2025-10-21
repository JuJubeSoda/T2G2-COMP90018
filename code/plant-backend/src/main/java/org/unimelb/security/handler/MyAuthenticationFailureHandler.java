package org.unimelb.security.handler;

import com.alibaba.fastjson2.JSON;
import org.unimelb.common.constant.ResultConstant;
import org.unimelb.common.vo.Result;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class MyAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        Result<Map<String, Object>> result;
        
        // 根据不同的异常类型返回不同的错误信息
        if (exception instanceof UsernameNotFoundException) {
            // 用户不存在
            result = Result.fail(ResultConstant.FAIL_USER_NOT_FOUND.getCode(), ResultConstant.FAIL_USER_NOT_FOUND.getMessage());
        } else if (exception instanceof BadCredentialsException) {
            // 密码错误
            result = Result.fail(ResultConstant.FAIL_WRONG_PASSWORD.getCode(), ResultConstant.FAIL_WRONG_PASSWORD.getMessage());
        } else {
            // 其他认证错误
            result = Result.fail(ResultConstant.FAIL_LOGIN_ERROR.getCode(), ResultConstant.FAIL_LOGIN_ERROR.getMessage());
        }
        
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(JSON.toJSONString(result));
    }
}