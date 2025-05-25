CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_email_trgm
    ON conversation_user USING gin (email gin_trgm_ops);
