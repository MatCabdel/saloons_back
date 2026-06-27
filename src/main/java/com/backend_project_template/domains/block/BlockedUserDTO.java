package com.backend_project_template.domains.block;

import java.time.LocalDateTime;

public class BlockedUserDTO {

    private Long id;
    private UserSummary blocker;
    private UserSummary blocked;
    private String reason;
    private String reasonDisplayName;
    private String description;
    private LocalDateTime createdAt;

    public BlockedUserDTO() {
    }

    public BlockedUserDTO(BlockedUser entity) {
        this.id = entity.getId();
        this.blocker = new UserSummary(
                entity.getBlocker().getId(),
                entity.getBlocker().getUserName(),
                entity.getBlocker().getImgUrl(),
                entity.getBlocker().getEmail());
        this.blocked = new UserSummary(
                entity.getBlocked().getId(),
                entity.getBlocked().getUserName(),
                entity.getBlocked().getImgUrl(),
                entity.getBlocked().getEmail());
        if (entity.getReason() != null) {
            this.reason = entity.getReason().name();
            this.reasonDisplayName = entity.getReason().getDisplayName();
        }
        this.description = entity.getDescription();
        this.createdAt = entity.getCreatedAt();
    }

    public Long getId() {
        return id;
    }

    public UserSummary getBlocker() {
        return blocker;
    }

    public UserSummary getBlocked() {
        return blocked;
    }

    public String getReason() {
        return reason;
    }

    public String getReasonDisplayName() {
        return reasonDisplayName;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public static class UserSummary {
        private Long id;
        private String userName;
        private String imgUrl;
        private String email;

        public UserSummary(Long id, String userName, String imgUrl, String email) {
            this.id = id;
            this.userName = userName;
            this.imgUrl = imgUrl;
            this.email = email;
        }

        public Long getId() {
            return id;
        }

        public String getUserName() {
            return userName;
        }

        public String getImgUrl() {
            return imgUrl;
        }

        public String getEmail() {
            return email;
        }
    }
}
