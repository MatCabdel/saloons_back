-- V1: Change auth_provider and profile_status from ENUM to VARCHAR
-- This is safe to run even if columns are already VARCHAR
ALTER TABLE `user`
MODIFY COLUMN `auth_provider` VARCHAR(20) DEFAULT 'EMAIL';

ALTER TABLE `user`
MODIFY COLUMN `profile_status` VARCHAR(30) DEFAULT 'PROFILE_INCOMPLETE';