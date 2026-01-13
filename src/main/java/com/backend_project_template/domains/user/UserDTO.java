package com.backend_project_template.domains.user;

public class UserDTO {

  private Long id;
  private String userName;
  private String email;
  private String imgUrl;
  private int age;
  private String city;
  private String description;
  private Long currentSaloonId;

  public UserDTO() {
  }

  public UserDTO(User user) {
    this.setId(user.getId());
    this.setEmail(user.getEmail());
    this.setUserName(user.getUserName());
    this.setImgUrl(user.getImgUrl());
    this.setCity(user.getCity());
    this.setDescription(user.getDescription());
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

  public static UserDTO fromEntity(User user) {
    UserDTO dto = new UserDTO();
    dto.setId(user.getId());
    dto.setUserName(user.getUserName());
    dto.setEmail(user.getEmail());
    dto.setImgUrl(user.getImgUrl());
    dto.setCity(user.getCity());
    dto.setDescription(user.getDescription());
    return dto;
  }
}
