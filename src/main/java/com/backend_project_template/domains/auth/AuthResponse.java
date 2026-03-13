package com.backend_project_template.domains.auth;

import com.backend_project_template.domains.user.UserDTO;

/**
 * DTO for authentication response containing user info and JWT token.
 */
public class AuthResponse {

    private UserDTO user;
    private String token;
    private boolean newUser;

    public AuthResponse() {
    }

    public AuthResponse(UserDTO user, String token, boolean newUser) {
        this.user = user;
        this.token = token;
        this.newUser = newUser;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isNewUser() {
        return newUser;
    }

    public void setNewUser(boolean newUser) {
        this.newUser = newUser;
    }
}
