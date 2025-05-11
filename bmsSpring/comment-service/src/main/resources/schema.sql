CREATE TABLE IF NOT EXISTS comment (
    id SERIAL PRIMARY KEY,
    body TEXT NOT NULL,
    title TEXT NOT NULL,
    user_likes BIGINT[] DEFAULT '{}',
    user_dislikes BIGINT[] DEFAULT '{}',
    reference_id BIGINT,
    user_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    images TEXT[] DEFAULT '{}',
    reference_type varchar(55) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_reference_id ON comment (reference_id);
CREATE INDEX IF NOT EXISTS idx_user_id_comment ON comment (user_id);
