package com.lolservice.dto;

public class AuthResponse {
    private boolean success;
    private String token;
    private UserDTO user;
    private String message;

    // Constructors
    public AuthResponse() {
    }

    public AuthResponse(String token, UserDTO user) {
        this.success = true;
        this.token = token;
        this.user = user;
    }

    public AuthResponse(String message) {
        this.success = false;
        this.message = message;
    }

    public AuthResponse(boolean success, String token, UserDTO user, String message) {
        this.success = success;
        this.token = token;
        this.user = user;
        this.message = message;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}