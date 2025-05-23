CREATE TABLE IF NOT EXISTS kanban_column
(
    id          SERIAL PRIMARY KEY,
    title       TEXT NOT NULL,
    user_id     BIGINT NOT NULL,
    order_index INT  NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS kanban_task
(
    id          SERIAL PRIMARY KEY,
    content     TEXT        NOT NULL,
    type        VARCHAR(50) NOT NULL,
    user_id     BIGINT NOT NULL,
    order_index INT         NOT NULL,
    column_id   BIGINT      NOT NULL REFERENCES kanban_column (id) ON DELETE CASCADE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_kanban_task_column_id ON kanban_task (column_id);

CREATE INDEX IF NOT EXISTS idx_kanban_task_user_id ON kanban_task (user_id);

CREATE INDEX IF NOT EXISTS idx_kanban_column_user_id ON kanban_column (user_id);