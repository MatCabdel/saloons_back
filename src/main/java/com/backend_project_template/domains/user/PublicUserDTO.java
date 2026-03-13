package com.backend_project_template.domains.user;

import java.time.LocalDate;
import java.time.Period;

/**
 * DTO public pour afficher le profil d'un autre utilisateur.
 * N'expose PAS les données sensibles (email, authProvider, role, lastLoginAt,
 * createdAt, birthDate, isPremium).
 */
public class PublicUserDTO {

  private Long id;
  private String userName;
  private String imgUrl;
  private int age;
  private String city;
  private String description;
  private String firstname;

  public PublicUserDTO() {
  }

  public PublicUserDTO(User user) {
    this.id = user.getId();
    this.userName = user.getUserName();
    this.imgUrl = user.getImgUrl();
    this.city = user.getCity();
    this.description = user.getDescription();
    this.firstname = user.getFirstName();
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

  public String getFirstname() {
    return firstname;
  }

  public void setFirstname(String firstname) {
    this.firstname = firstname;
  }
}
