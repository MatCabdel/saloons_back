ALTER TABLE blocked_users
    ADD COLUMN reason VARCHAR(50) NULL,
    ADD COLUMN description VARCHAR(500) NULL;
