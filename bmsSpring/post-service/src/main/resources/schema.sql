CREATE TABLE IF NOT EXISTS post
(
    id            SERIAL PRIMARY KEY,
    approved      BOOLEAN NOT NULL DEFAULT FALSE,
    body          TEXT    NOT NULL,
    title         TEXT    NOT NULL,
    user_likes    BIGINT[]         DEFAULT '{}',
    user_dislikes BIGINT[]         DEFAULT '{}',
    user_id       BIGINT  NOT NULL,
    tags          TEXT[]        DEFAULT '{}',
    created_at    TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    images        TEXT[]           DEFAULT '{}'
);

CREATE INDEX IF NOT EXISTS idx_user_id_post ON post (user_id);

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_post_title_trgm ON post USING GIN (title gin_trgm_ops);

CREATE INDEX if NOT EXISTS idx_post_tags ON post USING gin (tags);

CREATE INDEX IF NOT EXISTS idx_post_created_at ON post (created_at desc);


CREATE EXTENSION if NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS post_embedding
(
    id         serial PRIMARY KEY,
    entity_id  BIGINT    NOT NULL UNIQUE REFERENCES post (id) ON DELETE cascade,
    embedding  vector(1024),
    created_at TIMESTAMP NOT NULL DEFAULT current_timestamp,
    updated_at TIMESTAMP NOT NULL DEFAULT current_timestamp
);

CREATE INDEX IF NOT EXISTS hnsw_post ON post_embedding USING hnsw (embedding vector_ip_ops)
    WITH
    (m = 16, ef_construction = 64);

CREATE INDEX IF NOT EXISTS idx_post_user_likes_cardinality ON post (cardinality(user_likes));

CREATE TABLE IF NOT EXISTS post_view_count
(
    post_id     BIGINT REFERENCES post (id) ON DELETE cascade,
    view_count  BIGINT NOT NULL DEFAULT 0,
    access_date date   NOT NULL,
    PRIMARY KEY (post_id, access_date)
)
    PARTITION BY
        range (access_date);



CREATE TABLE IF NOT EXISTS post_likes
(
    master_id    BIGINT NOT NULL REFERENCES post (id) ON DELETE cascade,
    child_id     BIGINT NOT NULL,
    multiplicity BIGINT NOT NULL DEFAULT 1 CHECK (multiplicity <= 1),
    PRIMARY KEY (master_id, child_id)
);

CREATE TABLE IF NOT EXISTS post_dislikes
(
    master_id    BIGINT NOT NULL REFERENCES post (id) ON DELETE cascade,
    child_id     BIGINT NOT NULL,
    multiplicity BIGINT NOT NULL DEFAULT 1 CHECK (multiplicity <= 1),
    PRIMARY KEY (master_id, child_id)
);



-- CREATE OR REPLACE PROCEDURE ensure_table_partitions(months_ahead INTEGER,
--                                                     target_table TEXT,
--                                                     date_column TEXT,
--                                                     reference_column TEXT
-- )
--     LANGUAGE plpgsql
-- AS $$
-- DECLARE
--     current_month_start DATE := date_trunc('month', CURRENT_DATE);
--     partition_start DATE;
--     partition_end DATE;
--     partition_name TEXT;
--     i INTEGER := 0;
--     default_exists BOOLEAN;
-- BEGIN
--     WHILE i < months_ahead LOOP
--             partition_start := current_month_start + (i || ' months')::INTERVAL;
--             partition_end := partition_start + INTERVAL '1 month';
--             partition_name := format('%I_%s', target_table, to_char(partition_start, 'YYYY_MM'));
--
--             IF NOT EXISTS (
--                 SELECT FROM pg_tables WHERE tablename = partition_name
--             ) THEN
--                 EXECUTE format(
--                                                     'CREATE TABLE IF NOT EXISTS %I PARTITION OF %I
--                                                      FOR VALUES FROM (%L) TO (%L);',
--                                                     partition_name,
--                                                     target_table,
--                                                     partition_start,
--                                                     partition_end
--                                                     );
--             END IF;
--
--             RAISE NOTICE 'Created partition %', partition_name;
--
--             EXECUTE format('CREATE INDEX IF NOT EXISTS idx_%I_date ON %I (%I);',
--                                                        target_table,
--                                                        partition_name,
--                                                        date_column
--                                                 );
--
--             EXECUTE format('CREATE INDEX IF NOT EXISTS idx_%I_%I ON %I (%I);',
--                                                        target_table,
--                                                        reference_column,
--                                                        partition_name,
--                                                        reference_column
--                                                 );
--
--             RAISE NOTICE 'Created index for partition %', partition_name;
--
--             i := i + 1;
--         END LOOP;
--
--     SELECT EXISTS (
--         SELECT FROM pg_inherits
--                         JOIN pg_class ON pg_inherits.inhrelid = pg_class.oid
--         WHERE pg_inherits.inhparent =  format('%I', target_table)::regclass
--             AND relname =  format('%I_default', target_table)
--     ) INTO default_exists;
--
--     IF NOT default_exists THEN
--         EXECUTE 'CREATE TABLE IF NOT EXISTS  ' || target_table || '_default PARTITION OF ' || target_table || ' DEFAULT;';
--         RAISE NOTICE 'Created default partition for %', target_table;
--
--         EXECUTE ' CREATE INDEX IF NOT EXISTS idx_' || target_table || '_default_date ON ' || target_table || '_default (' || date_column || ');';
--         EXECUTE ' CREATE INDEX IF NOT EXISTS idx_' || target_table || '_default_' || reference_column || ' ON ' || target_table || '_default (' || reference_column || ');';
--         RAISE NOTICE 'Created index for default partition %', target_table;
--     END IF;
-- END;
-- $$;
-- CALL ensure_table_partitions(12, 'post_view_count', 'access_date', 'post_id');