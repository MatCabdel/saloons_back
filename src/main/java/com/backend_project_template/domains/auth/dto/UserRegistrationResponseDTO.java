package com.backend_project_template.domains.auth.dto;

import java.time.LocalDateTime;

public class UserRegistrationResponseDTO {

  private Long id;
  private String email;
  private String userName;
  private String imgUrl;
  private LocalDateTime profileImageUpdatedAt;
  private String description;
  private String city;

  public UserRegistrationResponseDTO() {}

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getImgUrl() {
    return imgUrl;
  }

  public void setImgUrl(String imgUrl) {
    this.imgUrl = imgUrl;
  }

  public LocalDateTime getProfileImageUpdatedAt() {
    return profileImageUpdatedAt;
  }

  public void setProfileImageUpdatedAt(LocalDateTime profileImageUpdatedAt) {
    this.profileImageUpdatedAt = profileImageUpdatedAt;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }
}
