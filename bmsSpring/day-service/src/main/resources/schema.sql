CREATE TABLE IF NOT EXISTS day
(
    id
    SERIAL
    PRIMARY
    KEY,
    body
    TEXT
    NOT
    NULL,
    title
    TEXT
    NOT
    NULL,
    user_likes
    BIGINT[]
    DEFAULT
    '{}',
    user_dislikes
    BIGINT[]
    DEFAULT
    '{}',
    user_id
    BIGINT,
    created_at
    TIMESTAMP
    DEFAULT
    CURRENT_TIMESTAMP,
    updated_at
    TIMESTAMP
    DEFAULT
    CURRENT_TIMESTAMP,
    type
    varchar
(
    255
) NOT NULL
    );

CREATE INDEX IF NOT EXISTS idx_day_user_id ON day (user_id);


CREATE TABLE IF NOT EXISTS meal
(
    id
    SERIAL
    PRIMARY
    KEY,
    created_at
    TIMESTAMP
    DEFAULT
    CURRENT_TIMESTAMP,
    updated_at
    TIMESTAMP
    DEFAULT
    CURRENT_TIMESTAMP,
    user_id
    BIGINT,
    recipes
    BIGINT[]
    default
    '{}',
    day_id
    BIGINT
    NOT
    NULL
    REFERENCES
    day
(
    id
) ON DELETE CASCADE,
    period varchar
(
    55
) NOT NULL

    );

create table if not exists day_calendar(
    id serial primary key,
    user_id bigint,
    day_id bigint not null references day(id) on delete cascade,
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp,
    custom_date timestamp not null,
    UNIQUE (user_id, day_id, custom_date)
);
create index if not exists idx_day_calendar_user_id on day_calendar(user_id);
CREATE INDEX if not exists idx_day_calendar_custom_date_user
    ON day_calendar (custom_date DESC, user_id);

CREATE INDEX IF NOT EXISTS idx_meal_user_id ON meal (user_id);
CREATE INDEX IF NOT EXISTS idx_meal_day_id ON meal (day_id);
CREATE
EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX IF NOT EXISTS idx_day_title_trgm ON day USING GIN (title gin_trgm_ops);


create
extension if not exists vector;
-- SET hnsw.ef_search = 200;
-- SET maintenance_work_mem = '2GB';
-- SET max_parallel_maintenance_workers = 3;
-- SET hnsw.iterative_scan = strict_order;

create table if not exists day_embedding
(
    id
    serial
    primary
    key,
    entity_id
    bigint
    not
    null
    unique
    references
    day
(
    id
) on delete cascade,
    embedding vector
(
    1024
),
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
    );

CREATE INDEX IF NOT EXISTS hnsw_day
    ON day_embedding USING hnsw (embedding vector_ip_ops) with (m=16, ef_construction =64 );