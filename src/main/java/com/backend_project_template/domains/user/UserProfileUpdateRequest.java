package com.backend_project_template.domains.user;

import jakarta.validation.constraints.Size;

public class UserProfileUpdateRequest {

  private static final int USERNAME_MIN_LENGTH = 2;
  private static final int USERNAME_MAX_LENGTH = 30;
  private static final int CITY_MAX_LENGTH = 100;
  private static final int DESCRIPTION_MAX_LENGTH = 500;

  @Size(min = USERNAME_MIN_LENGTH, max = USERNAME_MAX_LENGTH, message = "Le pseudo doit contenir entre 2 et 30 caractères")
  private String userName;

  @Size(max = CITY_MAX_LENGTH, message = "La ville ne doit pas dépasser 100 caractères")
  private String city;

  @Size(max = DESCRIPTION_MAX_LENGTH, message = "La description ne doit pas dépasser 500 caractères")
  private String description;

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
