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
  private int connectedCount;
  private LocalDateTime createdAt;
  private BigDecimal longitude;
  private BigDecimal latitude;
  private String address;
  private String city;
  private Integer radiusMeters;
  private List<UserDTO> usersInSaloon;
  private Boolean isPrivate;

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

  public int getConnectedCount() {
    return connectedCount;
  }

  public void setConnectedCount(int connectedCount) {
    this.connectedCount = connectedCount;
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

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  private SaloonType type;
  private String typeDisplayName;

  public SaloonType getType() {
    return type;
  }

  public void setType(SaloonType type) {
    this.type = type;
    if (type != null) {
      this.typeDisplayName = type.getDisplayName();
    }
  }

  public String getTypeDisplayName() {
    return typeDisplayName;
  }

  public void setTypeDisplayName(String typeDisplayName) {
    this.typeDisplayName = typeDisplayName;
  }

  public Integer getRadiusMeters() {
    return radiusMeters;
  }

  public void setRadiusMeters(Integer radiusMeters) {
    this.radiusMeters = radiusMeters;
  }

  public Boolean getIsPrivate() {
    return isPrivate;
  }

  public void setIsPrivate(Boolean isPrivate) {
    this.isPrivate = isPrivate;
  }

  private Boolean isActive;

  public Boolean getIsActive() {
    return isActive;
  }

  public void setIsActive(Boolean isActive) {
    this.isActive = isActive;
  }

  public List<UserDTO> getUsersInSaloon() {
    return usersInSaloon;
  }

  public void setUsersInSaloon(List<UserDTO> usersInSaloon) {
    this.usersInSaloon = usersInSaloon;
  }
}
