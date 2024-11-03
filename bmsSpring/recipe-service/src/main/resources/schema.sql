CREATE TABLE IF NOT EXISTS recipe
(
    id            SERIAL PRIMARY KEY,
    approved      BOOLEAN     NOT NULL DEFAULT FALSE,
    body          TEXT        NOT NULL,
    title         TEXT        NOT NULL,
    user_likes    BIGINT[]             DEFAULT '{}',
    user_dislikes BIGINT[]             DEFAULT '{}',
    user_id       BIGINT,
    created_at    TIMESTAMP            DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP            DEFAULT CURRENT_TIMESTAMP,
    images        TEXT[]               default '{}',
    videos        TEXT[]               default '{}',
    type          varchar(55) NOT NULL
);


CREATE TABLE IF NOT EXISTS ingredient_quantity
(
    id            SERIAL PRIMARY KEY,
    recipe_id     BIGINT  NOT NULL REFERENCES recipe (id) ON DELETE CASCADE,
    ingredient_id BIGINT  NOT NULL,
    quantity      NUMERIC NOT NULL check ( quantity > 0 ),
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ingredient_quantity_recipe_id ON ingredient_quantity (recipe_id);
CREATE INDEX IF NOT EXISTS idx_ingredient_quantity_ingredient_id ON ingredient_quantity (ingredient_id);
CREATE INDEX IF NOT EXISTS idx_recipe_user_id ON recipe (user_id);
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX IF NOT EXISTS idx_recipe_title_trgm ON recipe USING GIN (title gin_trgm_ops);