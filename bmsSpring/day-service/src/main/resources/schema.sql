CREATE TABLE IF NOT EXISTS DAY
(
    id            SERIAL PRIMARY KEY,
    body          TEXT         NOT NULL,
    title         TEXT         NOT NULL,
    user_likes    BIGINT[]  DEFAULT '{}',
    user_dislikes BIGINT[]  DEFAULT '{}',
    user_id       BIGINT       NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    type          VARCHAR(255) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_day_user_id ON DAY (user_id);

CREATE INDEX IF NOT EXISTS idx_day_user_likes_cardinality ON DAY (cardinality(user_likes));

CREATE INDEX IF NOT EXISTS idx_DAY_created_at ON DAY (created_at);

CREATE INDEX IF NOT EXISTS idx_day_type ON DAY (type);


CREATE TABLE IF NOT EXISTS Meal
(
    id         SERIAL PRIMARY KEY,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id    BIGINT      NOT NULL,
    recipes    BIGINT[]  DEFAULT '{}',
    day_id     BIGINT      NOT NULL REFERENCES DAY (id) ON DELETE CASCADE,
    period     VARCHAR(55) NOT NULL
);


CREATE TABLE IF NOT EXISTS day_calendar
(
    id          serial PRIMARY KEY,
    user_id     BIGINT    NOT NULL,
    day_id      BIGINT    NOT NULL REFERENCES DAY (id) ON DELETE cascade,
    created_at  TIMESTAMP DEFAULT current_timestamp,
    updated_at  TIMESTAMP DEFAULT current_timestamp,
    custom_date TIMESTAMP NOT NULL,
    UNIQUE (user_id, day_id, custom_date)
);

CREATE INDEX if NOT EXISTS idx_day_calendar_user_id ON day_calendar (user_id);

CREATE INDEX if NOT EXISTS id_day_calendar_id_user_id_day_id ON day_calendar (id, user_id, day_id);


CREATE INDEX if NOT EXISTS idx_day_calendar_custom_date_user ON day_calendar (custom_date DESC, user_id);

CREATE INDEX IF NOT EXISTS idx_meal_user_id ON meal (user_id);

CREATE INDEX IF NOT EXISTS idx_meal_day_id ON meal (day_id);

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_day_title_trgm ON DAY USING GIN (title gin_trgm_ops);

CREATE EXTENSION if NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS day_embedding
(
    id         serial PRIMARY KEY,
    entity_id  BIGINT    NOT NULL UNIQUE REFERENCES DAY (id) ON DELETE cascade,
    embedding  vector(1024),
    created_at TIMESTAMP NOT NULL DEFAULT current_timestamp,
    updated_at TIMESTAMP NOT NULL DEFAULT current_timestamp
);

CREATE INDEX IF NOT EXISTS hnsw_day ON day_embedding USING hnsw (embedding vector_ip_ops)
    WITH
    (m = 16, ef_construction = 64);


CREATE TABLE IF NOT EXISTS day_likes
(
    master_id    BIGINT NOT NULL REFERENCES day (id) ON DELETE cascade,
    child_id     BIGINT NOT NULL,
    multiplicity BIGINT NOT NULL DEFAULT 1 CHECK (multiplicity <= 1),
    PRIMARY KEY (master_id, child_id)
);

CREATE TABLE IF NOT EXISTS day_dislikes
(
    master_id    BIGINT NOT NULL REFERENCES day (id) ON DELETE cascade,
    child_id     BIGINT NOT NULL,
    multiplicity BIGINT NOT NULL DEFAULT 1 CHECK (multiplicity <= 1),
    PRIMARY KEY (master_id, child_id)
);


CREATE TABLE IF NOT EXISTS meal_recipes
(
    master_id    BIGINT NOT NULL REFERENCES meal (id) ON DELETE cascade,
    child_id     BIGINT NOT NULL,
    multiplicity BIGINT NOT NULL DEFAULT 1,
    PRIMARY KEY (master_id, child_id)
);