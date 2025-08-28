package org.unimelb.security.handler;

import com.alibaba.fastjson2.JSON;
import org.unimelb.common.utils.JwtUtil;
import org.unimelb.common.vo.Result;
import org.unimelb.security.vo.SecurityUser;
import org.unimelb.user.entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MyAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Autowired
    private JwtUtil jwtUtil;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // 获取登录的用户对象
        SecurityUser securityUser =  (SecurityUser)authentication.getPrincipal();

        // 通过jwt创建token
        User user = securityUser.getUser();
        user.setAvatarData(null);
        String token = jwtUtil.createJwt(user);

        // 返回result
        Result<Object> result = Result.success(token,"Login success");

        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(JSON.toJSONString(result));

    }
}