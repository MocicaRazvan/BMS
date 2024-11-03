CREATE TABLE IF NOT EXISTS ingredient
(
    id         SERIAL PRIMARY KEY,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id    BIGINT      NOT NULL,
    name       TEXT        NOT NULL UNIQUE,
    type       VARCHAR(55) NOT NULL,
    display    BOOLEAN     NOT NULL
);

CREATE TABLE IF NOT EXISTS nutritional_fact
(
    id            SERIAL PRIMARY KEY,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id       BIGINT      NOT NULL,
--     calories NUMERIC NOT NULL,
    fat           NUMERIC     NOT NULL check ( fat >= 0),
    saturated_fat NUMERIC     NOT NULL CHECK (saturated_fat >= 0) CHECK ( saturated_fat <= fat),
    carbohydrates NUMERIC     NOT NULL CHECK (carbohydrates >= 0),
    sugar         NUMERIC     NOT NULL CHECK (sugar >= 0) CHECK ( sugar <= carbohydrates),
    protein       NUMERIC     NOT NULL CHECK (protein >= 0),
    salt          NUMERIC     NOT NULL CHECK (salt >= 0),
    unit          VARCHAR(25) NOT NULL,
    ingredient_id BIGINT      NOT NULL UNIQUE REFERENCES ingredient (id) ON DELETE CASCADE
        CHECK (fat + protein + carbohydrates + salt > 0)
);
CREATE INDEX IF NOT EXISTS idx_user_id_ingredient ON ingredient (user_id);
CREATE INDEX IF NOT EXISTS idx_ingredient_name_trgm ON ingredient USING GIN (name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_ingredient_type ON ingredient (type);
CREATE INDEX IF NOT EXISTS idx_nutritional_fact_ingredient_id ON nutritional_fact (ingredient_id);
CREATE INDEX IF NOT EXISTS idx_nutritional_fact_user_id ON nutritional_fact (user_id);



