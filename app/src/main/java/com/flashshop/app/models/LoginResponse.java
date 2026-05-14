package com.flashshop.app.models;

import com.google.gson.annotations.SerializedName;

/**
 * Unified login payload from {@code login.php} (form or JSON). Customer rows include
 * {@code user}; admin rows include {@code admin}.
 */
public class LoginResponse {
    @SerializedName("status") public String status;
    @SerializedName("message") public String message;
    @SerializedName("token") public String token;
    @SerializedName("user") public User user;
    @SerializedName("admin") public AdminBrief admin;

    public boolean isSuccess() {
        return "success".equals(status);
    }
}
