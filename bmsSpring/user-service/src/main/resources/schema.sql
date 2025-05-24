CREATE TABLE IF NOT EXISTS user_custom
(
    id                SERIAL PRIMARY KEY,
    first_name        VARCHAR(255) NOT NULL,
    last_name         VARCHAR(255) NOT NULL,
    email             VARCHAR(255) NOT NULL UNIQUE,
    password          VARCHAR(255),
    role              VARCHAR(50)  NOT NULL DEFAULT 'ROLE_USER' CHECK (role IN ('ROLE_ADMIN', 'ROLE_USER', 'ROLE_TRAINER')),
    image             TEXT,
    created_at        TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    provider          VARCHAR(50)  NOT NULL DEFAULT 'LOCAL',
    is_email_verified BOOLEAN               DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS jwt_token
(
    id         SERIAL PRIMARY KEY,
    token      VARCHAR(1024) NOT NULL UNIQUE,
    revoked    BOOLEAN       NOT NULL,
    user_id    BIGINT REFERENCES user_custom (id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS otp_token
(
    id                 SERIAL PRIMARY KEY,
    token              VARCHAR(1024) NOT NULL,
    user_id            BIGINT REFERENCES user_custom (id) ON DELETE CASCADE,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_in_seconds BIGINT        NOT NULL,
    type               VARCHAR(50)   NOT NULL
);

CREATE TABLE IF NOT EXISTS oauth_state
(
    id            SERIAL PRIMARY KEY,
    state         VARCHAR(255) NOT NULL UNIQUE,
    code_verifier VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_user_id_jwt_token ON jwt_token (user_id);

-- CREATE INDEX IF NOT EXISTS idx_user_id_otp_token ON otp_token (user_id);

CREATE INDEX IF NOT EXISTS idx_user_custom_created_at ON user_custom (created_at desc);

-- CREATE INDEX if NOT EXISTS idx_user_custom_role ON user_custom USING btree (role);

CREATE INDEX if NOT EXISTS idx_user_custom_email_role ON user_custom USING btree (email, role);

CREATE INDEX IF NOT EXISTS idx_user_custom_provider ON user_custom USING btree (provider);

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_user_custom_email_trgm ON user_custom USING gin (email gin_trgm_ops);

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_otp_user_type ON otp_token USING btree (user_id, type);

CREATE EXTENSION if NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS user_embedding
(
    id         serial PRIMARY KEY,
    entity_id  BIGINT    NOT NULL UNIQUE REFERENCES user_custom (id) ON DELETE cascade,
    embedding  vector(1024),
    created_at TIMESTAMP NOT NULL DEFAULT current_timestamp,
    updated_at TIMESTAMP NOT NULL DEFAULT current_timestamp
);

CREATE INDEX IF NOT EXISTS hnsw_user ON user_embedding USING hnsw (embedding vector_ip_ops)
    WITH
    (m = 16, ef_construction = 64);