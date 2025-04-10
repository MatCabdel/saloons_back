package com.backend_project_template.domains.saloon;

import com.backend_project_template.domains.saloonSession.SaloonSession;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Saloon {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

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

  @OneToMany(mappedBy = "saloon")
  private List<SaloonSession> saloonSessions;

  public int getId() {
    return id;
  }

  public void setId(int id) {
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

  public List<SaloonSession> getSaloonSessions() {
    return saloonSessions;
  }

  public void setSaloonSessions(List<SaloonSession> saloonSessions) {
    this.saloonSessions = saloonSessions;
  }
}
