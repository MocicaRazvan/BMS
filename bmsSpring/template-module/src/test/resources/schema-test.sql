create table if not exists id_generated(
    id         SERIAL PRIMARY KEY,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

create table if not exists many_to_one_user(
    id         SERIAL PRIMARY KEY,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id    BIGINT NOT NULL
);

create table if not exists associative_entity(
    master_id BIGINT NOT NULL,
    child_id  BIGINT NOT NULL,
    multiplicity BIGINT NOT NULL DEFAULT 1,
    primary key (master_id, child_id)
)