package org.unimelb.security.filter;

import org.unimelb.common.utils.JwtUtil;
import org.unimelb.common.context.UserContext;
import org.unimelb.security.vo.SecurityUser;
import org.unimelb.user.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();
        if (!uri.endsWith("/user/login")
                && !uri.endsWith("/user/sms/login")
                && !uri.endsWith("/user/avatar")
                && !uri.contains("/user/info")
                && !uri.contains("/survey/examination")
                && !uri.endsWith("/user/logout")
                && !uri.endsWith("/user/reg")
                && !uri.contains("/sms")
        ) {

            String token = request.getHeader("Authorization");
            if (StringUtils.hasLength(token)) {
                User user = null;
                try {
                    user = jwtUtil.parseJwt(token, User.class);
                    if (user != null) {

                        SecurityUser securityUser = new SecurityUser(user);
                        SecurityContextHolder.getContext().setAuthentication(
                                new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities())
                        );

                        UserContext.setUser(user);
                    }
                } catch (Exception e) {

                    throw new RuntimeException(e);
                }
            }
        }

        filterChain.doFilter(request, response);
        

        UserContext.clear();

    }
}