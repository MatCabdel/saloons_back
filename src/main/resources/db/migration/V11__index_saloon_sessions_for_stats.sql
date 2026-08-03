CREATE INDEX idx_saloon_sessions_saloon_connected
    ON saloon_sessions (saloon_id, connected_at);

CREATE INDEX idx_saloon_sessions_user_saloon_open
    ON saloon_sessions (user_id, saloon_id, disconnected_at);
