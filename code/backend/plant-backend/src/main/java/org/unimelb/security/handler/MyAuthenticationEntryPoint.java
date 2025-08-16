package org.unimelb.security.handler;

import com.alibaba.fastjson2.JSON;
import org.unimelb.common.constant.ResultConstant;
import org.unimelb.common.vo.Result;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MyAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setContentType("application/json;charset=utf-8");
        Result<Object> result = Result.fail(ResultConstant.FAIL_UNLOGIN_ERROR.getCode(),
                ResultConstant.FAIL_UNLOGIN_ERROR.getMessage());
        response.getWriter().write(JSON.toJSONString(result));
    }
}