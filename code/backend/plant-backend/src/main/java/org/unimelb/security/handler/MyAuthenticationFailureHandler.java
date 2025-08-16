package org.unimelb.security.handler;

import com.alibaba.fastjson2.JSON;
import org.unimelb.common.constant.ResultConstant;
import org.unimelb.common.vo.Result;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class MyAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        Result<Map<String, Object>> result = Result.fail(ResultConstant.FAIL_LOGIN_ERROR.getCode(), ResultConstant.FAIL_LOGIN_ERROR.getMessage());
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(JSON.toJSONString(result));
    }
}