CREATE TABLE task_shares (
    todo_id BIGINT NOT NULL,
    token VARCHAR(36) NOT NULL,
    created_by BIGINT NOT NULL,
    expires_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (token),
    CONSTRAINT fk_task_shares_todo
        FOREIGN KEY (todo_id) REFERENCES todos(id),
    INDEX idx_task_shares_todo_expires (todo_id, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
