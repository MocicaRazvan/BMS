create TABLE IF NOT EXISTS user_cart
(
    id         SERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL unique,
    plan_ids   BIGINT[]  DEFAULT '{}',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
