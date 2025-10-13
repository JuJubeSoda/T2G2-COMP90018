package org.unimelb.test.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查和数据库连接测试控制器
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    /**
     * 简单的健康检查
     */
    @GetMapping
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "Plant World Backend");
        return response;
    }

    /**
     * 数据库连接测试
     */
    @GetMapping("/db")
    public Map<String, Object> databaseHealth() {
        Map<String, Object> response = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            // 测试连接
            boolean isValid = connection.isValid(3);
            
            response.put("status", isValid ? "UP" : "DOWN");
            response.put("database", "connected");
            response.put("url", connection.getMetaData().getURL());
            response.put("timestamp", LocalDateTime.now());
            
        } catch (SQLException e) {
            response.put("status", "DOWN");
            response.put("database", "disconnected");
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now());
        }
        
        return response;
    }

    /**
     * 完整系统状态检查
     */
    @GetMapping("/status")
    public Map<String, Object> systemStatus() {
        Map<String, Object> response = new HashMap<>();
        
        // 应用状态
        response.put("application", "UP");
        
        // 数据库状态
        try (Connection connection = dataSource.getConnection()) {
            boolean dbValid = connection.isValid(3);
            response.put("database", dbValid ? "UP" : "DOWN");
            response.put("dbUrl", connection.getMetaData().getURL());
        } catch (SQLException e) {
            response.put("database", "DOWN");
            response.put("dbError", e.getMessage());
        }
        
        response.put("timestamp", LocalDateTime.now());
        response.put("overallStatus", response.get("database").equals("UP") ? "HEALTHY" : "UNHEALTHY");
        
        return response;
    }
}
