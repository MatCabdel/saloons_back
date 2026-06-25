package com.backend_project_template.domains.block;

import java.time.LocalDateTime;

public class BlockedUserDTO {

    private Long id;
    private UserSummary blocker;
    private UserSummary blocked;
    private LocalDateTime createdAt;

    public BlockedUserDTO() {
    }

    public BlockedUserDTO(BlockedUser entity) {
        this.id = entity.getId();
        this.blocker = new UserSummary(
                entity.getBlocker().getId(),
                entity.getBlocker().getUserName(),
                entity.getBlocker().getImgUrl());
        this.blocked = new UserSummary(
                entity.getBlocked().getId(),
                entity.getBlocked().getUserName(),
                entity.getBlocked().getImgUrl());
        this.createdAt = entity.getCreatedAt();
    }

    public Long getId() { return id; }
    public UserSummary getBlocker() { return blocker; }
    public UserSummary getBlocked() { return blocked; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public static class UserSummary {
        private Long id;
        private String userName;
        private String imgUrl;

        public UserSummary(Long id, String userName, String imgUrl) {
            this.id = id;
            this.userName = userName;
            this.imgUrl = imgUrl;
        }

        public Long getId() { return id; }
        public String getUserName() { return userName; }
        public String getImgUrl() { return imgUrl; }
    }
}
