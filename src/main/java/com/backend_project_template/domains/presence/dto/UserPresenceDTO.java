package com.backend_project_template.domains.presence.dto;

/**
 * DTO pour les informations d'un utilisateur dans la présence.
 */
@SuppressWarnings("checkstyle:ParameterNumber")
public class UserPresenceDTO {
    private Long id;
    private String userName;
    private String imgUrl;
    private Integer age;
    private String city;

    public UserPresenceDTO() {
    }

    public UserPresenceDTO(Long id, String userName, String imgUrl, Integer age, String city) {
        this.id = id;
        this.userName = userName;
        this.imgUrl = imgUrl;
        this.age = age;
        this.city = city;
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

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
