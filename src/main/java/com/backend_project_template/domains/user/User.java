package com.backend_project_template.domains.user;

import com.backend_project_template.domains.saloon.Saloon;
import com.backend_project_template.domains.saloonSession.SaloonSession;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "\"user\"")
public class User implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String email;
  private String password;
  private String firstName;
  private String lastName;
  private String userName;
  private String description;
  private LocalDate birthDate;
  private String city;
  private String postalCode;
  private LocalDateTime lastLoginAt;
  private LocalDateTime createdAt;
  private Boolean isPremium = false;
  private LocalDateTime premiumStartDate;
  private LocalDateTime premiumEndDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "profile_status", columnDefinition = "VARCHAR(30) DEFAULT 'PROFILE_INCOMPLETE'")
  private ProfileStatus profileStatus = ProfileStatus.PROFILE_INCOMPLETE;

  private String firebaseUid;

  @Enumerated(EnumType.STRING)
  @Column(name = "auth_provider", columnDefinition = "VARCHAR(20) DEFAULT 'EMAIL'")
  private AuthProvider authProvider = AuthProvider.EMAIL;

  @ElementCollection(fetch = FetchType.EAGER)
  private Set<String> roles = new HashSet<>();

  private String imgUrl;
  private LocalDateTime profileImageUpdatedAt;

  @ManyToOne
  @JoinColumn(name = "current_saloon_id")
  @JsonBackReference
  private Saloon currentSaloon;

  @OneToMany(mappedBy = "user")
  @JsonManagedReference("user-session")
  private List<SaloonSession> saloonSessions;

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

  @Override
  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
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

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  public LocalDateTime getLastLoginAt() {
    return lastLoginAt;
  }

  public void setLastLoginAt(LocalDateTime lastLoginAt) {
    this.lastLoginAt = lastLoginAt;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  @PrePersist
  protected void onCreate() {
    if (this.createdAt == null) {
      this.createdAt = LocalDateTime.now();
    }
  }

  public Boolean getIsPremium() {
    return isPremium;
  }

  public void setIsPremium(Boolean isPremium) {
    this.isPremium = isPremium;
  }

  public LocalDateTime getPremiumStartDate() {
    return premiumStartDate;
  }

  public void setPremiumStartDate(LocalDateTime premiumStartDate) {
    this.premiumStartDate = premiumStartDate;
  }

  public LocalDateTime getPremiumEndDate() {
    return premiumEndDate;
  }

  public void setPremiumEndDate(LocalDateTime premiumEndDate) {
    this.premiumEndDate = premiumEndDate;
  }

  public Set<String> getRoles() {
    return roles;
  }

  public void setRoles(Set<String> roles) {
    this.roles = roles;
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

  public Saloon getCurrentSaloon() {
    return currentSaloon;
  }

  public void setCurrentSaloon(Saloon currentSaloon) {
    this.currentSaloon = currentSaloon;
  }

  public List<SaloonSession> getSaloonSessions() {
    return saloonSessions;
  }

  public void setSaloonSessions(List<SaloonSession> saloonSessions) {
    this.saloonSessions = saloonSessions;
  }

  public ProfileStatus getProfileStatus() {
    return profileStatus;
  }

  public void setProfileStatus(ProfileStatus profileStatus) {
    this.profileStatus = profileStatus;
  }

  public String getFirebaseUid() {
    return firebaseUid;
  }

  public void setFirebaseUid(String firebaseUid) {
    this.firebaseUid = firebaseUid;
  }

  public AuthProvider getAuthProvider() {
    return authProvider;
  }

  public void setAuthProvider(AuthProvider authProvider) {
    this.authProvider = authProvider;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
