package com.backend_project_template.domains.user;

/**
 * DTO pour la requête de mise à jour du profil utilisateur.
 */
public class UserProfileUpdateRequest {
    private String userName;
    private String city;
    private String description;

    public UserProfileUpdateRequest() {
    }

    public UserProfileUpdateRequest(String userName, String city, String description) {
        this.userName = userName;
        this.city = city;
        this.description = description;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
