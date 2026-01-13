package com.backend_project_template.domains.saloon;

import com.backend_project_template.domains.saloonSession.SaloonSession;
import com.backend_project_template.domains.user.User;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Saloon {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String imgUrl;

  @Column(nullable = false)
  private int visitorNumber;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private BigDecimal longitude;

  @Column(nullable = false)
  private BigDecimal latitude;

  private String address;

  private String city;

  private String country;

  /** Rayon par défaut du saloon en mètres. */
  private static final int DEFAULT_RADIUS_METERS = 100;

  @Column(nullable = false)
  private Integer radiusMeters = DEFAULT_RADIUS_METERS;

  @Column(nullable = false)
  private Boolean isActive = true;

  @OneToMany(mappedBy = "saloon")
  @JsonManagedReference
  private List<SaloonSession> saloonSessions;

  @OneToMany(mappedBy = "currentSaloon")
  private List<User> usersInSaloon;

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

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public Integer getRadiusMeters() {
    return radiusMeters;
  }

  public void setRadiusMeters(Integer radiusMeters) {
    this.radiusMeters = radiusMeters;
  }

  public Boolean getIsActive() {
    return isActive;
  }

  public void setIsActive(Boolean isActive) {
    this.isActive = isActive;
  }

  public List<SaloonSession> getSaloonSessions() {
    return saloonSessions;
  }

  public void setSaloonSessions(List<SaloonSession> saloonSessions) {
    this.saloonSessions = saloonSessions;
  }

  public List<User> getUsersInSaloon() {
    return usersInSaloon;
  }

  public void setUsersInSaloon(List<User> usersInSaloon) {
    this.usersInSaloon = usersInSaloon;
  }
}
