-- V3__Create_SFTP_Server_Configuration_Table.sql

CREATE TABLE IF NOT EXISTS sftp_server_configuration (
    id UUID PRIMARY KEY,
    port INT NOT NULL,
    key_path VARCHAR(255) NOT NULL,
    host_key_algorithm VARCHAR(24) NOT NULL,
    max_write_data_packet_length INT NOT NULL,
    max_login_attempt_threshold INT NOT NULL,
    delay_between_login_attempts INT NOT NULL,
    core_pool_size INT NOT NULL,
    max_pool_size INT NOT NULL,
    keep_alive_time INT NOT NULL,
    queue_capacity INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_date TIMESTAMP NOT NULL
);