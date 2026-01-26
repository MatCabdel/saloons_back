package com.backend_project_template.domains.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetPasswordRequest {

    private static final int MIN_PASSWORD_LENGTH = 8;

    @NotBlank(message = "Token is required")
    private String token;

    @NotBlank(message = "Password is required")
    @Size(min = MIN_PASSWORD_LENGTH, message = "Password must be at least 8 characters")
    private String newPassword;

    public ResetPasswordRequest() {}

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
