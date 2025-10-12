package org.unimelb.test.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.unimelb.common.vo.Result;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据库连接测试控制器
 */
@RestController
@RequestMapping("/test")
public class DatabaseTestController {

    @Autowired
    private DataSource dataSource;

    /**
     * 测试数据库连接
     */
    @GetMapping("/db-connection")
    public Result<Map<String, Object>> testDatabaseConnection() {
        Map<String, Object> result = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            // 测试连接是否有效
            boolean isValid = connection.isValid(5); // 5秒超时
            
            result.put("status", "success");
            result.put("connected", isValid);
            result.put("database", connection.getMetaData().getDatabaseProductName());
            result.put("version", connection.getMetaData().getDatabaseProductVersion());
            result.put("url", connection.getMetaData().getURL());
            result.put("username", connection.getMetaData().getUserName());
            result.put("timestamp", LocalDateTime.now());
            
            return Result.success(result);
            
        } catch (SQLException e) {
            result.put("status", "error");
            result.put("connected", false);
            result.put("error", e.getMessage());
            result.put("errorCode", e.getErrorCode());
            result.put("timestamp", LocalDateTime.now());
            
            return Result.fail("数据库连接失败: " + e.getMessage(), result);
        }
    }

    /**
     * 测试数据库查询
     */
    @GetMapping("/db-query")
    public Result<Map<String, Object>> testDatabaseQuery() {
        Map<String, Object> result = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            // 执行简单查询
            String sql = "SELECT NOW() as current_time, version() as db_version";
            
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                
                if (resultSet.next()) {
                    result.put("status", "success");
                    result.put("currentTime", resultSet.getString("current_time"));
                    result.put("dbVersion", resultSet.getString("db_version"));
                    result.put("timestamp", LocalDateTime.now());
                    
                    return Result.success(result);
                } else {
                    result.put("status", "error");
                    result.put("message", "查询结果为空");
                    return Result.fail("查询失败", result);
                }
            }
            
        } catch (SQLException e) {
            result.put("status", "error");
            result.put("error", e.getMessage());
            result.put("errorCode", e.getErrorCode());
            result.put("timestamp", LocalDateTime.now());
            
            return Result.fail("数据库查询失败: " + e.getMessage(), result);
        }
    }

    /**
     * 测试用户表查询
     */
    @GetMapping("/db-users")
    public Result<Map<String, Object>> testUserTable() {
        Map<String, Object> result = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            // 查询用户表
            String sql = "SELECT COUNT(*) as user_count FROM user_table";
            
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                
                if (resultSet.next()) {
                    result.put("status", "success");
                    result.put("userCount", resultSet.getInt("user_count"));
                    result.put("timestamp", LocalDateTime.now());
                    
                    return Result.success(result);
                } else {
                    result.put("status", "error");
                    result.put("message", "用户表查询结果为空");
                    return Result.fail("查询失败", result);
                }
            }
            
        } catch (SQLException e) {
            result.put("status", "error");
            result.put("error", e.getMessage());
            result.put("errorCode", e.getErrorCode());
            result.put("timestamp", LocalDateTime.now());
            
            return Result.fail("用户表查询失败: " + e.getMessage(), result);
        }
    }

    /**
     * 获取数据库信息
     */
    @GetMapping("/db-info")
    public Result<Map<String, Object>> getDatabaseInfo() {
        Map<String, Object> result = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            var metaData = connection.getMetaData();
            
            result.put("status", "success");
            result.put("databaseProductName", metaData.getDatabaseProductName());
            result.put("databaseProductVersion", metaData.getDatabaseProductVersion());
            result.put("driverName", metaData.getDriverName());
            result.put("driverVersion", metaData.getDriverVersion());
            result.put("url", metaData.getURL());
            result.put("username", metaData.getUserName());
            result.put("maxConnections", metaData.getMaxConnections());
            result.put("timestamp", LocalDateTime.now());
            
            return Result.success(result);
            
        } catch (SQLException e) {
            result.put("status", "error");
            result.put("error", e.getMessage());
            result.put("errorCode", e.getErrorCode());
            result.put("timestamp", LocalDateTime.now());
            
            return Result.fail("获取数据库信息失败: " + e.getMessage(), result);
        }
    }
}
