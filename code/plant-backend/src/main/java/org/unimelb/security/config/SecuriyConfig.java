package org.unimelb.security.config;

import org.unimelb.security.filter.JsonUsernamePasswordAuthenticationFilter;
import org.unimelb.security.filter.JwtAuthenticationFilter;
import org.unimelb.security.handler.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.unimelb.security.handler.*;

@EnableWebSecurity
@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecuriyConfig {

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Autowired
    private MyLogoutSuccessHandler myLogoutSuccessHandler;

    @Autowired
    private MyAuthenticationEntryPoint myAuthenticationEntryPoint;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private MyAccessDeniedHandler myAccessDeniedHandler;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 1. 放行部分请求
        http.authorizeHttpRequests(request -> {
            request.requestMatchers("/user/login","/user/sms/login",
                            "/user/info",
                            "/user/logout",
                            "/user/reg",
                            "/user/avatar",
                            "/survey/examination/**",
                            "/swagger-ui/**",
                            "/swagger-resources/**",
                            "/v3/**",
                            "/test/**",
                            "/sms/**").anonymous()
                    .anyRequest().authenticated(); // 其余请求都需认证
        });
        // 2. 登录请求url
        /*http.formLogin(form -> {
            form.loginProcessingUrl("/user/login")
                    .successHandler(new AuthenticationSuccessHandler() {
                        @Override
                        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
                            response.getWriter().write("login success");
                        }
                    });
        });*/
        // 3. 前后端分离，配置为无状态
        http.sessionManagement(session -> {
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        });
        // 4. 关闭csrf
        http.csrf(csrf -> {
            csrf.disable();
        });
        // 5. 跨域
        http.cors(cors -> {});

        http.logout(logout -> {
            logout.logoutUrl("/user/logout")
                    .logoutSuccessHandler(myLogoutSuccessHandler);
        });

        http.exceptionHandling(exception -> {
            exception.authenticationEntryPoint(myAuthenticationEntryPoint)
                    .accessDeniedHandler(myAccessDeniedHandler);
        });

        // 注册JsonUsernamePasswordAuthenticationFilter
        http.addFilterAt(jsonUsernamePasswordAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        http.addFilterBefore(jwtAuthenticationFilter,UsernamePasswordAuthenticationFilter.class);

        return  http.build();
    }

    @Autowired
    private AuthenticationConfiguration authenticationConfiguration;

    @Autowired
    private MyAuthenticationSuccessHandler myAuthenticationSuccessHandler;
    @Autowired
    private MyAuthenticationFailureHandler myAuthenticationFailureHandler;


    public JsonUsernamePasswordAuthenticationFilter jsonUsernamePasswordAuthenticationFilter() throws Exception {
        JsonUsernamePasswordAuthenticationFilter filter = new JsonUsernamePasswordAuthenticationFilter();

        filter.setAuthenticationManager(authenticationConfiguration.getAuthenticationManager());
        filter.setFilterProcessesUrl("/user/login");
        filter.setAuthenticationSuccessHandler(myAuthenticationSuccessHandler);
        filter.setAuthenticationFailureHandler(myAuthenticationFailureHandler);
        return filter;
    }
}
