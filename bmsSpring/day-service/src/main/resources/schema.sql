CREATE TABLE IF NOT EXISTS day
(
    id            SERIAL PRIMARY KEY,
    body          TEXT         NOT NULL,
    title         TEXT         NOT NULL,
    user_likes    BIGINT[]  DEFAULT '{}',
    user_dislikes BIGINT[]  DEFAULT '{}',
    user_id       BIGINT,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    type          varchar(255) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_day_user_id ON day (user_id);


CREATE TABLE IF NOT EXISTS meal
(
    id         SERIAL PRIMARY KEY,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id    BIGINT,
    recipes    BIGINT[]  default '{}',
    day_id     BIGINT      NOT NULL REFERENCES day (id) ON DELETE CASCADE,
    period     varchar(55) NOT NULL

);

CREATE INDEX IF NOT EXISTS idx_meal_user_id ON meal (user_id);

