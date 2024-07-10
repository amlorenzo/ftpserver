-- V1__Create_SFTPUser_table.sql

CREATE TABLE IF NOT EXISTS sftp_users (
     id UUID PRIMARY KEY,
     username VARCHAR(25) NOT NULL UNIQUE,
     password VARCHAR(100),
     directory VARCHAR(255),
     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
     company_id INT NOT NULL,
     company_name VARCHAR(255) NOT NULL,
     public_key TEXT,
     ticket_url VARCHAR(255) NOT NULL,
     enabled BOOLEAN NOT NULL DEFAULT TRUE,
     last_login TIMESTAMP NULL
    );