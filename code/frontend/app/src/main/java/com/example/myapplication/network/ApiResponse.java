package com.example.myapplication.network;

import com.google.gson.annotations.SerializedName;

/**
 * A generic wrapper for API responses that matches the common backend structure.
 * {
 *   "code": 200,
 *   "message": "Success",
 *   "data": { ... } or [ ... ],
 *   "success": true
 * }
 * @param <T> The type of the data payload.
 */
public class ApiResponse<T> {

    @SerializedName("code")
    private int code;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private T data;

    @SerializedName("success")
    private boolean success;

    // Default constructor
    public ApiResponse() {}

    // Getters and Setters
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Check if the API call was successful
     */
    public boolean isSuccessful() {
        return success && code == 200;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", success=" + success +
                '}';
    }
}
