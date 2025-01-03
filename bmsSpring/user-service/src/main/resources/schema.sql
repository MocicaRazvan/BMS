CREATE TABLE IF NOT EXISTS user_custom
(
    id
    SERIAL
    PRIMARY
    KEY,
    first_name
    VARCHAR
(
    255
) NOT NULL,
    last_name VARCHAR
(
    255
) NOT NULL,
    email VARCHAR
(
    255
) UNIQUE NOT NULL UNIQUE,
    password VARCHAR
(
    255
),
    role varchar
(
    50
) NOT NULL DEFAULT 'ROLE_USER',
    image TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    provider VARCHAR
(
    50
) NOT NULL DEFAULT 'LOCAL',
    is_email_verified BOOLEAN DEFAULT FALSE
    );

CREATE TABLE IF NOT EXISTS jwt_token
(
    id
    SERIAL
    PRIMARY
    KEY,
    token
    VARCHAR
(
    1024
) NOT NULL UNIQUE,
    revoked BOOLEAN NOT NULL,
    user_id BIGINT REFERENCES user_custom
(
    id
) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS otp_token
(
    id
    SERIAL
    PRIMARY
    KEY,
    token
    VARCHAR
(
    1024
) NOT NULL,
    user_id BIGINT REFERENCES user_custom
(
    id
) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_in_seconds BIGINT NOT NULL,
    type VARCHAR
(
    50
) NOT NULL
    );

CREATE TABLE IF NOT EXISTS oauth_state
(
    id
    SERIAL
    PRIMARY
    KEY,
    state
    VARCHAR
(
    255
) NOT NULL UNIQUE,
    code_verifier VARCHAR
(
    255
) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_user_id_jwt_token ON jwt_token (user_id);
CREATE INDEX IF NOT EXISTS idx_user_id_otp_token ON otp_token (user_id);
CREATE
EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX IF NOT EXISTS idx_user_custom_email_trgm ON user_custom USING gin (email gin_trgm_ops);
CREATE
EXTENSION IF NOT EXISTS pg_trgm;

create table if not exists user_embedding
(
    id
    serial
    primary
    key,
    entity_id
    bigint
    not
    null
    unique
    references
    user_custom
(
    id
) on delete cascade,
    embedding vector
(
    1024
),
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
    );

CREATE INDEX IF NOT EXISTS hnsw_user
    ON user_embedding USING hnsw (embedding vector_ip_ops) with (m=16, ef_construction =64 );