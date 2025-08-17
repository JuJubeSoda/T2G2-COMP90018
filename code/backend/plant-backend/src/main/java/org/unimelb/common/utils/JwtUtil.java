package org.unimelb.common.utils;

import com.alibaba.fastjson2.JSON;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
@Slf4j
public class JwtUtil {
    
    @Value("${plant.jwt.expire}")  // 有效期
    private long jwtExpire;
    
    @Value("${plant.jwt.secret}")  // 令牌秘钥
    private String jwtSecret;

    public String createJwt(Object data) {
        // 当前时间
        long currentTime = System.currentTimeMillis();
        // 过期时间
        long expTime = currentTime + (jwtExpire * 60 * 1000);
        // 构建jwt
        JwtBuilder builder = Jwts.builder()
                .setId(UUID.randomUUID() + "")
                .setSubject(JSON.toJSONString(data))
                .setIssuer("system")
                .setIssuedAt(new Date(currentTime))
                .setExpiration(new Date(expTime))
                .signWith(encodeSecret(jwtSecret), SignatureAlgorithm.HS256);
        return builder.compact();
    }

    private SecretKey encodeSecret(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public Boolean validJwt(String jwt) {
        try {
            jwt = jwt.replace("Bearer ","");
            Jwts.parserBuilder()
                    .setSigningKey(encodeSecret(jwtSecret))
                    .build()
                    .parseClaimsJws(jwt);
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
        return true;
    }

    public <T> T parseJwt(String jwt, Class<T> clazz) {
        jwt = jwt.replace("Bearer ","");
        Claims body = Jwts.parserBuilder()
                .setSigningKey(encodeSecret(jwtSecret))
                .build()
                .parseClaimsJws(jwt)
                .getBody();
        return JSON.parseObject(body.getSubject(), clazz);
    }

}