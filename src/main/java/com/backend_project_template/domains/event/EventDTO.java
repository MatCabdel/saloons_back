package com.backend_project_template.domains.event;

import java.time.LocalDateTime;

public class EventDTO {

    private Long id;
    private String title;
    private String subTitle;
    private String imageUrl;
    private String description;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Integer radiusMeters;
    private Long saloonId;
    private String saloonName;
    private String saloonImgUrl;
    private String saloonAddress;
    private String saloonCity;
    private Double saloonLatitude;
    private Double saloonLongitude;
    private Integer saloonRadiusMeters;
    private String saloonType;
    private Boolean saloonIsPrivate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isActive;
    private Long interestedCount;
    private Boolean isInterested;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public Integer getRadiusMeters() {
        return radiusMeters;
    }

    public void setRadiusMeters(Integer radiusMeters) {
        this.radiusMeters = radiusMeters;
    }

    public Long getSaloonId() {
        return saloonId;
    }

    public void setSaloonId(Long saloonId) {
        this.saloonId = saloonId;
    }

    public String getSaloonName() {
        return saloonName;
    }

    public void setSaloonName(String saloonName) {
        this.saloonName = saloonName;
    }

    public String getSaloonImgUrl() {
        return saloonImgUrl;
    }

    public void setSaloonImgUrl(String saloonImgUrl) {
        this.saloonImgUrl = saloonImgUrl;
    }

    public String getSaloonAddress() {
        return saloonAddress;
    }

    public void setSaloonAddress(String saloonAddress) {
        this.saloonAddress = saloonAddress;
    }

    public String getSaloonCity() {
        return saloonCity;
    }

    public void setSaloonCity(String saloonCity) {
        this.saloonCity = saloonCity;
    }

    public Double getSaloonLatitude() {
        return saloonLatitude;
    }

    public void setSaloonLatitude(Double saloonLatitude) {
        this.saloonLatitude = saloonLatitude;
    }

    public Double getSaloonLongitude() {
        return saloonLongitude;
    }

    public void setSaloonLongitude(Double saloonLongitude) {
        this.saloonLongitude = saloonLongitude;
    }

    public Integer getSaloonRadiusMeters() {
        return saloonRadiusMeters;
    }

    public void setSaloonRadiusMeters(Integer saloonRadiusMeters) {
        this.saloonRadiusMeters = saloonRadiusMeters;
    }

    public String getSaloonType() {
        return saloonType;
    }

    public void setSaloonType(String saloonType) {
        this.saloonType = saloonType;
    }

    public Boolean getSaloonIsPrivate() {
        return saloonIsPrivate;
    }

    public void setSaloonIsPrivate(Boolean saloonIsPrivate) {
        this.saloonIsPrivate = saloonIsPrivate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Long getInterestedCount() {
        return interestedCount;
    }

    public void setInterestedCount(Long interestedCount) {
        this.interestedCount = interestedCount;
    }

    public Boolean getIsInterested() {
        return isInterested;
    }

    public void setIsInterested(Boolean isInterested) {
        this.isInterested = isInterested;
    }
}
