CREATE TABLE IF NOT EXISTS address
(
    id            SERIAL PRIMARY KEY,
    created_at    TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    city          TEXT    NOT NULL,
    country       TEXT    NOT NULL,
    line1         TEXT    NOT NULL,
    line2         TEXT,
    postal_code   TEXT    NOT NULL,
    state         TEXT    NOT NULL
);
CREATE TABLE IF NOT EXISTS custom_order
(
    id            SERIAL PRIMARY KEY,
    user_id       BIGINT,
    created_at    TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    address_id    BIGINT NOT NULL REFERENCES address(id),
    plan_ids      BIGINT[] NOT NULL DEFAULT '{}',
    stripe_invoice_id TEXT,
    total        NUMERIC NOT NULL check ( total > 0 )
);