package com.example.myapplication.network;

import com.google.gson.annotations.SerializedName;

/**
 * A generic wrapper for API responses that matches the common backend structure.
 * {
 *   "code": 200,
 *   "message": "Success",
 *   "data": { ... } or [ ... ]
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

    // Getters
    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
