package com.backend_project_template.domains.admin.stats;

/**
 * Item classé avec nom et valeur (pour top saloons, villes, etc.).
 */
public class RankedItem {
    private Long id;
    private String name;
    private long value;

    public RankedItem() {
    }

    public RankedItem(Long id, String name, long value) {
        this.id = id;
        this.name = name;
        this.value = value;
    }

    public RankedItem(String name, long value) {
        this.name = name;
        this.value = value;
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

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }
}
