package com.backend_project_template.domains.saloon;

import com.backend_project_template.domains.user.UserDTO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class SaloonDTO {

  private Long id;
  private String name;
  private String imgUrl;
  private int visitorNumber;
  private LocalDateTime createdAt;
  private BigDecimal longitude;
  private BigDecimal latitude;
  private String address;
  private List<UserDTO> usersInSaloon;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getImgUrl() {
    return imgUrl;
  }

  public void setImgUrl(String imgUrl) {
    this.imgUrl = imgUrl;
  }

  public int getVisitorNumber() {
    return visitorNumber;
  }

  public void setVisitorNumber(int visitorNumber) {
    this.visitorNumber = visitorNumber;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public BigDecimal getLongitude() {
    return longitude;
  }

  public void setLongitude(BigDecimal longitude) {
    this.longitude = longitude;
  }

  public BigDecimal getLatitude() {
    return latitude;
  }

  public void setLatitude(BigDecimal latitude) {
    this.latitude = latitude;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public List<UserDTO> getUsersInSaloon() {
    return usersInSaloon;
  }

  public void setUsersInSaloon(List<UserDTO> usersInSaloon) {
    this.usersInSaloon = usersInSaloon;
  }
}
