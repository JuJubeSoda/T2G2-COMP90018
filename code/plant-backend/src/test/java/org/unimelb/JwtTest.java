package org.unimelb;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.unimelb.ai.controller.OpenAIController;
import org.unimelb.ai.service.OpenAIService;
import org.unimelb.common.utils.JwtUtil;
import org.unimelb.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;

@SpringBootTest
@MockBean(OpenAIService.class)
@MockBean(OpenAIController.class)
@MockBean(DataSource.class)
public class JwtTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    public void test1(){
        User user = new User();
        user.setUsername("zhangsan");
        user.setPhone("18899998888");
        String jwt = jwtUtil.createJwt(user);
        System.out.println(jwt);
    }

//    @Test
//    public void test2(){
//        String jwt = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI5NDdkMjA4ZS1kZTQzLTRiYzYtYTYyMi02MjAwNzlkODE1YmYiLCJzdWIiOiJ7XCJhY2NvdW50Tm9uRXhwaXJlZFwiOnRydWUsXCJhY2NvdW50Tm9uTG9ja2VkXCI6dHJ1ZSxcImF1dGhvcml0aWVzXCI6W3tcImF1dGhvcml0eVwiOlwiUk9MRV9hZG1pblwifV0sXCJjcmVkZW50aWFsc05vbkV4cGlyZWRcIjp0cnVlLFwiZW5hYmxlZFwiOnRydWUsXCJwYXNzd29yZFwiOlwiJDJhJDEwJGtpNEdDOFpLNGtIWEdwbUFoWWozdk9oS0J4NFR5bS91Z2VOcHF4MklZa283ZWtETXhlNEVXXCJ9IiwiaXNzIjoic3lzdGVtIiwiaWF0IjoxNzEwNjUzMzAxLCJleHAiOjE3MTA2NTY5MDF9.pTKwnohdOPb-nQJwt2XZvpmP12eCkg5pzlEoWiVhAeI";
//        User user = jwtUtil.parseJwt(jwt, User.class);
//        System.out.println(user);
//    }
}