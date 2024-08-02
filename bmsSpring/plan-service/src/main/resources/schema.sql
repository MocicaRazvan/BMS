CREATE TABLE IF NOT EXISTS plan
(
    id            SERIAL PRIMARY KEY,
    approved      BOOLEAN NOT NULL DEFAULT FALSE,
    body          TEXT    NOT NULL,
    title         TEXT    NOT NULL,
    user_likes    BIGINT[]         DEFAULT '{}',
    user_dislikes BIGINT[]         DEFAULT '{}',
    user_id       BIGINT,
    created_at    TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    images        TEXT[]           default '{}',
    price        NUMERIC NOT NULL check ( price > 0 ),
    type          varchar(55) NOT NULL,
    recipes       BIGINT[]           default '{}',
    display       BOOLEAN NOT NULL DEFAULT FALSE
);
