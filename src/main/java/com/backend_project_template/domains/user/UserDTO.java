package com.backend_project_template.domains.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.Period;

public class UserDTO {

  private Long id;
  private String userName;
  private String email;
  private String imgUrl;
  private int age;
  private String city;
  private String description;
  private Long currentSaloonId;
  private String profileStatus;
  private String authProvider;
  private String firstname;
  private String lastname;
  private Boolean isPremium;
  private String role;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private LocalDate birthDate;

  public UserDTO() {
  }

  public UserDTO(User user) {
    this.id = user.getId();
    this.email = user.getEmail();
    this.userName = user.getUserName();
    this.imgUrl = user.getImgUrl();
    this.description = user.getDescription();
    this.city = user.getCity();
    this.birthDate = user.getBirthDate();
    this.currentSaloonId = user.getCurrentSaloon() != null ? user.getCurrentSaloon().getId() : null;
    this.profileStatus = user.getProfileStatus() != null
        ? user.getProfileStatus().name()
        : ProfileStatus.PROFILE_INCOMPLETE.name();
    this.authProvider = user.getAuthProvider() != null
        ? user.getAuthProvider().name()
        : AuthProvider.EMAIL.name();
    this.firstname = user.getFirstName();
    this.lastname = user.getLastName();
    this.isPremium = user.getIsPremium();
    this.role = user.getRoles() != null && !user.getRoles().isEmpty()
        ? user.getRoles().iterator().next()
        : null;
    if (user.getBirthDate() != null) {
      this.age = Period.between(user.getBirthDate(), LocalDate.now()).getYears();
    }
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getImgUrl() {
    return imgUrl;
  }

  public void setImgUrl(String imgUrl) {
    this.imgUrl = imgUrl;
  }

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }

  public Long getCurrentSaloonId() {
    return currentSaloonId;
  }

  public void setCurrentSaloonId(Long currentSaloonId) {
    this.currentSaloonId = currentSaloonId;
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

  public LocalDate getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(LocalDate birthDate) {
    this.birthDate = birthDate;
  }

  public String getProfileStatus() {
    return profileStatus;
  }

  public void setProfileStatus(String profileStatus) {
    this.profileStatus = profileStatus;
  }

  public String getAuthProvider() {
    return authProvider;
  }

  public void setAuthProvider(String authProvider) {
    this.authProvider = authProvider;
  }

  public String getFirstname() {
    return firstname;
  }

  public void setFirstname(String firstname) {
    this.firstname = firstname;
  }

  public String getLastname() {
    return lastname;
  }

  public void setLastname(String lastname) {
    this.lastname = lastname;
  }

  public Boolean getIsPremium() {
    return isPremium;
  }

  public void setIsPremium(Boolean isPremium) {
    this.isPremium = isPremium;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public static UserDTO fromEntity(User user) {
    return new UserDTO(user);
  }
}
