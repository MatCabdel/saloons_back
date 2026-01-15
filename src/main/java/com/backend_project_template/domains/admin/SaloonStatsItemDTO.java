package com.backend_project_template.domains.admin;

public class SaloonStatsItemDTO {
    private Long id;
    private String name;
    private String city;
    private String imgUrl;
    private int connectedCount;

    public SaloonStatsItemDTO() {
    }

    public SaloonStatsItemDTO(Long id, String name, String city, String imgUrl, int connectedCount) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.imgUrl = imgUrl;
        this.connectedCount = connectedCount;
    }

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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public int getConnectedCount() {
        return connectedCount;
    }

    public void setConnectedCount(int connectedCount) {
        this.connectedCount = connectedCount;
    }
}
