package com.example.myapplication.network;

import com.google.gson.JsonElement;

/**
 * BaseResponse - Generic response wrapper for legacy API endpoints.
 * 
 * Structure:
 * - code: HTTP status code or custom error code
 * - msg: Human-readable message or error description
 * - data: Flexible JSON data (parsed with JsonElement)
 * 
 * Used by: Authentication, AI Bot, Plant AI, and older endpoints
 * 
 * Note: Newer endpoints use ApiResponse<T> for better type safety.
 */
public class BaseResponse {
    /** Response code (e.g., 200 for success, 401 for unauthorized) */
    public Integer code;
    
    /** Response message (e.g., "Success", "Invalid credentials") */
    public String msg;
    
    /** Response data as flexible JSON element */
    public JsonElement data;
}
