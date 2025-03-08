CREATE TABLE IF NOT EXISTS plan
(
    id
              SERIAL
        PRIMARY
            KEY,
    approved
              BOOLEAN
                          NOT
                              NULL
                                   DEFAULT
                                       FALSE,
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
    images
              TEXT[]
                                   default
                                       '{}',
    price
              NUMERIC
                          NOT
                              NULL
        check
            (
            price >
            0
            ),
    type      varchar(55) NOT NULL,
    days      BIGINT[]             default '{}',
    display   BOOLEAN     NOT NULL DEFAULT FALSE,
    objective varchar(55) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_user_id_plan ON plan (user_id);
CREATE
    EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX IF NOT EXISTS idx_plan_title_trgm ON plan USING GIN (title gin_trgm_ops);


create
    extension if not exists vector;

-- SET hnsw.ef_search = 200;
-- SET maintenance_work_mem = '2GB';
-- SET max_parallel_maintenance_workers = 3;
-- SET hnsw.iterative_scan = strict_order;

create table if not exists plan_embedding
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
            plan
                (
                 id
                    ) on delete cascade,
    embedding  vector(1024),
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);

CREATE INDEX IF NOT EXISTS hnsw_plan
    ON plan_embedding USING hnsw (embedding vector_ip_ops) with (m=16, ef_construction =64 );