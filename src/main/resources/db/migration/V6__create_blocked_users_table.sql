CREATE TABLE blocked_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    blocker_user_id BIGINT NOT NULL,
    blocked_user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_block_blocker FOREIGN KEY (blocker_user_id) REFERENCES `user` (id) ON DELETE CASCADE,
    CONSTRAINT fk_block_blocked FOREIGN KEY (blocked_user_id) REFERENCES `user` (id) ON DELETE CASCADE,
    CONSTRAINT uq_block UNIQUE (
        blocker_user_id,
        blocked_user_id
    )
);