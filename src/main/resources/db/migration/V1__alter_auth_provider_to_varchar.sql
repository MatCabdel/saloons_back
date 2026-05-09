-- V1: Change auth_provider from ENUM to VARCHAR to support new providers without schema changes
ALTER TABLE `user`
    MODIFY COLUMN `auth_provider` VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'EMAIL';
