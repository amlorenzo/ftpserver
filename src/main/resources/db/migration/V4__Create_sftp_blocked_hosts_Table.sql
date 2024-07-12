--V4__Create_sftp_blocked_hosts_Table.sql

CREATE TABLE IF NOT EXISTS sftp_blocked_hosts (
    id UUID PRIMARY KEY,
    ip_address VARCHAR(255) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    username_attempted VARCHAR(255) NOT NULL,
    blocked_at TIMESTAMP NOT NULL,
    allow BOOLEAN NOT NULL
);