CREATE TABLE IF NOT EXISTS post
(
    id            SERIAL PRIMARY KEY,
    approved      BOOLEAN NOT NULL DEFAULT FALSE,
    body          TEXT    NOT NULL,
    title         TEXT    NOT NULL,
    user_likes    BIGINT[]         DEFAULT '{}',
    user_dislikes BIGINT[]         DEFAULT '{}',
    user_id       BIGINT,
    tags          TEXT[],
    created_at    TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    images        TEXT[]           default '{}'
);

CREATE INDEX IF NOT EXISTS idx_user_id_post ON post (user_id);
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX IF NOT EXISTS idx_post_title_trgm ON post USING GIN (title gin_trgm_ops);


