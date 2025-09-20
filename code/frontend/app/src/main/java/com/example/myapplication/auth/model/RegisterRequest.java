package com.example.myapplication.auth.model;

public class RegisterRequest {
    public String username;
    public String phone;
    public String password;
    public String email;

    public RegisterRequest(String username, String phone, String password, String email) {
        this.username = username;
        this.phone = phone;
        this.password = password;
        this.email = email;
    }
}
