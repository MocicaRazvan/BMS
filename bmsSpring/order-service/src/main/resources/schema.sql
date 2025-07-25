CREATE TABLE IF NOT EXISTS address
(
    id          SERIAL PRIMARY KEY,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    city        TEXT NOT NULL,
    country     TEXT NOT NULL,
    line1       TEXT NOT NULL,
    line2       TEXT,
    postal_code TEXT NOT NULL,
    state       TEXT NOT NULL
);
CREATE TABLE IF NOT EXISTS custom_order
(
    id                SERIAL PRIMARY KEY,
    user_id           BIGINT   NOT NULL,
    created_at        TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    address_id        BIGINT   NOT NULL REFERENCES address (id),
    plan_ids          BIGINT[] NOT NULL DEFAULT '{}',
    stripe_invoice_id TEXT,
    total             NUMERIC  NOT NULL CHECK (total > 0)
);
CREATE TABLE IF NOT EXISTS plan_order
(
    id              SERIAL PRIMARY KEY,
    price           NUMERIC     NOT NULL CHECK (price > 0),
    type            varchar(55) NOT NULL,
    objective       varchar(55) NOT NULL,
    plan_id         BIGINT      NOT NULL,
    plan_created_at TIMESTAMP   NOT NULL,
    plan_updated_at TIMESTAMP   NOT NULL,
    order_id        BIGINT      NOT NULL REFERENCES custom_order (id),
    title           TEXT        NOT NULL,
    user_id         BIGINT      NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_custom_order_user_id ON custom_order (user_id);
CREATE INDEX IF NOT EXISTS idx_custom_order_address_id ON custom_order (address_id);
CREATE INDEX IF NOT EXISTS idx_custom_order_created_at ON custom_order using brin (created_at)
    with (autosummarize = 1 );

CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX IF NOT EXISTS idx_order_city_trgm ON address USING GIN (city gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_order_country_trgm ON address USING GIN (country gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_order_state_trgm ON address USING GIN (state gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_plan_order_user_id ON plan_order (user_id);
CREATE INDEX IF NOT EXISTS idx_plan_order_order_id ON plan_order (order_id);
