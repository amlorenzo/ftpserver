-- V1__Create_SFTPUser_table.sql

CREATE TABLE IF NOT EXISTS sftp_users (
     id UUID PRIMARY KEY,
     username VARCHAR(25) NOT NULL UNIQUE,
     password VARCHAR(100),
     directory VARCHAR(255),
     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
     company_id INT NOT NULL,
     company_name VARCHAR(255) NOT NULL,
     ticket_url VARCHAR(255) NOT NULL,
     enabled BOOLEAN NOT NULL DEFAULT TRUE,
     last_login TIMESTAMP NULL,
     password_login_enabled BOOLEAN NOT NULL DEFAULT TRUE,
     public_key_login_enabled BOOLEAN NOT NULL DEFAULT FALSE,
     last_password_change TIMESTAMP NULL
    );

CREATE TABLE IF NOT EXISTS public_keys (
       id UUID PRIMARY KEY,
       public_key TEXT NOT NULL,
       sftp_user_id UUID NOT NULL,
       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
       FOREIGN KEY (sftp_user_id) REFERENCES sftp_users(id) ON DELETE CASCADE
    );