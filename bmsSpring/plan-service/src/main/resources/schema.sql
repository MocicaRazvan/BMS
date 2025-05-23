CREATE TABLE IF NOT EXISTS plan
(
    id            SERIAL PRIMARY KEY,
    approved      BOOLEAN     NOT NULL DEFAULT FALSE,
    body          TEXT        NOT NULL,
    title         TEXT        NOT NULL,
    user_likes    BIGINT[]             DEFAULT '{}',
    user_dislikes BIGINT[]             DEFAULT '{}',
    user_id       BIGINT      NOT NULL,
    created_at    TIMESTAMP            DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP            DEFAULT CURRENT_TIMESTAMP,
    images        TEXT[]               DEFAULT '{}',
    price         NUMERIC     NOT NULL CHECK (price > 0),
    type          VARCHAR(55) NOT NULL,
    days          BIGINT[]             DEFAULT '{}',
    display       BOOLEAN     NOT NULL DEFAULT FALSE,
    objective     VARCHAR(55) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_user_id_plan ON plan (user_id);

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_plan_title_trgm ON plan USING GIN (title gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_plan_user_likes_cardinality ON plan (cardinality(user_likes));

CREATE INDEX IF NOT EXISTS idx_plan_created_at ON plan (created_at desc);



CREATE EXTENSION if NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS plan_embedding
(
    id         serial PRIMARY KEY,
    entity_id  BIGINT    NOT NULL UNIQUE REFERENCES plan (id) ON DELETE cascade,
    embedding  vector(1024),
    created_at TIMESTAMP NOT NULL DEFAULT current_timestamp,
    updated_at TIMESTAMP NOT NULL DEFAULT current_timestamp
);

CREATE INDEX IF NOT EXISTS hnsw_plan ON plan_embedding USING hnsw (embedding vector_ip_ops)
    WITH
    (m = 16, ef_construction = 64);


CREATE TABLE IF NOT EXISTS plan_likes
(
    master_id    BIGINT NOT NULL REFERENCES plan (id) ON DELETE cascade,
    child_id     BIGINT NOT NULL,
    multiplicity BIGINT NOT NULL DEFAULT 1 CHECK (multiplicity <= 1),
    PRIMARY KEY (master_id, child_id)
);

CREATE TABLE IF NOT EXISTS plan_dislikes
(
    master_id    BIGINT NOT NULL REFERENCES plan (id) ON DELETE cascade,
    child_id     BIGINT NOT NULL,
    multiplicity BIGINT NOT NULL DEFAULT 1 CHECK (multiplicity <= 1),
    PRIMARY KEY (master_id, child_id)
);

CREATE TABLE IF NOT EXISTS plan_days
(
    master_id    BIGINT NOT NULL REFERENCES plan (id) ON DELETE cascade,
    child_id     BIGINT NOT NULL,
    multiplicity BIGINT NOT NULL DEFAULT 1,
    PRIMARY KEY (master_id, child_id)
);

