package com.example.myapplication.network;

import com.google.gson.annotations.SerializedName;

/**
 * User model representing user data from the backend API.
 * 
 * Used by:
 * - GET /user - Get all users
 * 
 * Purpose:
 * - Maps userId to username for displaying plant discoverers
 */
public class User {
    
    @SerializedName("userId")
    private Long userId;
    
    @SerializedName("username")
    private String username;
    
    @SerializedName("phone")
    private String phone;
    
    @SerializedName("email")
    private String email;
    
    @SerializedName("createdAt")
    private String createdAt;
    
    @SerializedName("updatedAt")
    private String updatedAt;
    
    @SerializedName("userType")
    private String userType;
    
    @SerializedName("avatar")
    private String avatar;
    
    @SerializedName("avatarData")
    private String avatarData;
    
    // Constructors
    public User() {}
    
    // Getters
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public String getUserType() { return userType; }
    public String getAvatar() { return avatar; }
    public String getAvatarData() { return avatarData; }
    
    // Setters
    public void setUserId(Long userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public void setUserType(String userType) { this.userType = userType; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public void setAvatarData(String avatarData) { this.avatarData = avatarData; }
}
