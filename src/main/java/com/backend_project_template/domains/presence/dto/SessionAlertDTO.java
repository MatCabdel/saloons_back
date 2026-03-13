package com.backend_project_template.domains.presence.dto;

/**
 * DTO pour les alertes de session (expiration proche, session expirée).
 */
public class SessionAlertDTO {
    private String type; // SESSION_EXPIRING_SOON, SESSION_EXPIRED
    private Long saloonId;
    private Integer remainingMinutes;
    private String message;

    public SessionAlertDTO() {
    }

    public SessionAlertDTO(String type, Long saloonId, Integer remainingMinutes) {
        this.type = type;
        this.saloonId = saloonId;
        this.remainingMinutes = remainingMinutes;
        this.message = buildMessage(type, remainingMinutes);
    }

    private String buildMessage(String type, Integer remainingMinutes) {
        if ("SESSION_EXPIRING_SOON".equals(type)) {
            return "Votre session expire dans " + remainingMinutes + " minutes";
        } else if ("SESSION_EXPIRED".equals(type)) {
            return "Votre session a expiré";
        }
        return null;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getSaloonId() {
        return saloonId;
    }

    public void setSaloonId(Long saloonId) {
        this.saloonId = saloonId;
    }

    public Integer getRemainingMinutes() {
        return remainingMinutes;
    }

    public void setRemainingMinutes(Integer remainingMinutes) {
        this.remainingMinutes = remainingMinutes;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
